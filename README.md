```bash
cd ~/tools/kafka/kafka_2.13-3.4.0
```

```bash
bin/kafka-server-start.sh config/kraft/server.properties
```

```bash
bin/kafka-console-producer.sh --topic order.created --bootstrap-server localhost:9092
```
