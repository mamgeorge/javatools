https://kafka.apache.org/quickstart

	kafka_info.md
	The steps below work for shell only.
	For the java implementation, zookeeper & kafkaServer must be running.

0 install

	had to download 7Zip to unpack tgz & tar file
		installed in:
		C:\app\kafka\kafka_2.13-3.2.0

1 start shell for zookeeper

	> cd C:\app\kafka\kafka_2.13-3.2.0\bin\windows
	> zookeeper-server-start ../../config/zookeeper.properties

2 start shell for kafka server

	> cd C:\app\kafka\kafka_2.13-3.2.0\bin\windows
	> kafka-server-start ../../config/server.properties
		(note: may need to clear logs)

3 start shell to handle topics & producer

	> cd C:\app\kafka\kafka_2.13-3.2.0\bin\windows
	> kafka-topics --create --topic quickstart-events --bootstrap-server localhost:9092
		(note: this may already exist)

	> kafka-topics --describe --topic quickstart-events --bootstrap-server localhost:9092

	> kafka-console-producer --topic quickstart-events --bootstrap-server localhost:9092
	> qqq
	> www
	> eee

4 start shell to consume messages

	> cd C:\app\kafka\kafka_2.13-3.2.0\bin\windows
	> kafka-console-consumer --topic quickstart-events --from-beginning --bootstrap-server localhost:9092