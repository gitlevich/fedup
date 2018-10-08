package com.fedup.common.machinery

import org.apache.kafka.clients.consumer.*
import org.apache.kafka.streams.*
import org.apache.kafka.streams.state.*
import org.rocksdb.*
import java.util.*

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

data class KafkaStreamsConfig(val props: Properties)


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