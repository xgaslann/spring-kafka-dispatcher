cd ~/tools/kafka/kafka_2.13-3.4.0
bin/kafka-server-start.sh config/kraft/server.properties

bin/kafka-console-producer.sh --topic order.created --bootstrap-server localhost:9092