package com.fedup.shared.machinery

import com.fedup.shared.protocol.*
import org.apache.kafka.clients.consumer.*
import org.apache.kafka.clients.producer.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.state.*
import org.awaitility.Awaitility.*
import org.rocksdb.*
import java.util.*
import java.util.concurrent.*

fun createStreamsConfig(serviceId: String, bootstrapServers: String, stateDir: String, enableExactlyOnceSemantics: Boolean): KafkaStreamsConfig {
    val props = Properties()
    props[StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG] = CustomRocksDBConfig::class.java
    props[StreamsConfig.APPLICATION_ID_CONFIG] = serviceId
    props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
    props[StreamsConfig.STATE_DIR_CONFIG] = stateDir
    props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
    props[StreamsConfig.PROCESSING_GUARANTEE_CONFIG] = if (enableExactlyOnceSemantics) "exactly_once" else "at_least_once"
    props[StreamsConfig.COMMIT_INTERVAL_MS_CONFIG] = 1 //commit as fast as possible

    return KafkaStreamsConfig(props)
}

data class KafkaStreamsConfig(val props: Properties) {
    val bootstrapServers = props[StreamsConfig.BOOTSTRAP_SERVERS_CONFIG].toString()
}


/**
 * Stolen from Confluent people
 */
class CustomRocksDBConfig : RocksDBConfigSetter {

    override fun setConfig(storeName: String, options: Options,
                           configs: Map<String, Any>) {
        // Workaround: We must ensure that the parallelism is set to >= 2.  There seems to be a known
        // issue with RocksDB where explicitly setting the parallelism to 1 causes issues (even though
        // 1 seems to be RocksDB's default for this configuration).
        val compactionParallelism = Math.max(Runtime.getRuntime().availableProcessors(), 2)
        // Set number of compaction threads (but not flush threads).
        options.setIncreaseParallelism(compactionParallelism)
    }
}

fun <K, V> send(records: List<ProducerRecord<K, V>>, topic: Topic<K, V>) {
    KafkaProducer(
        createStreamsConfig(
            "${topic.name}_service",
            "localhost:29092",
            "/tmp/kafka-streams",
            true
        ).props,
        topic.keySerde.serializer(),
        topic.valueSerde.serializer()
    ).use { eventProducer ->
        records.forEach { record ->
            eventProducer
                .send(record)
                .get()
        }
    }
}

fun <K, V> readOne(topic: Topic<K, V>, bootstrapServers: String): List<KeyValue<K, V>> = read(1, topic, bootstrapServers)

fun <K, V> read(numberToRead: Int, topic: Topic<K, V>, bootstrapServers: String): List<KeyValue<K, V>> {
    val consumerConfig = Properties()
    consumerConfig[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
    consumerConfig[ConsumerConfig.GROUP_ID_CONFIG] = "Test-Reader-${UUID.randomUUID()}"
    consumerConfig[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

    val consumer = KafkaConsumer(consumerConfig, topic.keySerde.deserializer(), topic.valueSerde.deserializer())
    consumer.subscribe(listOf(topic.name))

    val actualValues = ArrayList<KeyValue<K, V>>()
    await().atMost(2, TimeUnit.SECONDS).until {
        val records = consumer.poll(100)
        records.mapTo(actualValues) { KeyValue.pair<K, V>(it.key(), it.value()) }
        actualValues.size >= numberToRead
    }
    consumer.close()
    return actualValues
}