#!/usr/bin/env bash

kafka-topics --create --topic user-locations --zookeeper localhost:2181 --partitions 1 --replication-factor 1
kafka-topics --create --topic driver-requests --zookeeper localhost:2181 --partitions 1 --replication-factor 1
kafka-topics --create --topic available-drivers --zookeeper localhost:2181 --partitions 1 --replication-factor 1