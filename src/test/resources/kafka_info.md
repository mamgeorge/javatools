https://kafka.apache.org/quickstart

install

	had to download 7Zip to unpack tgz & tar file
		installed in:
		C:\app\kafka\kafka_2.13-3.2.0

start shell for zookeeper

	> cd C:\app\kafka\kafka_2.13-3.2.0\bin\windows
	> zookeeper-server-start ../../config/zookeeper.properties

start shell for kafka server

	> cd C:\app\kafka\kafka_2.13-3.2.0\bin\windows
	> kafka-server-start ../../config/server.properties

start shell to handle topic

	> cd C:\app\kafka\kafka_2.13-3.2.0\bin\windows
	> kafka-topics --create --topic quickstart-events --bootstrap-server localhost:9092
	> kafka-topics --describe --topic quickstart-events --bootstrap-server localhost:9092

	> kafka-console-producer --topic quickstart-events --bootstrap-server localhost:9092
	> qqq
	> www
	> eee

	> kafka-console-consumer --topic quickstart-events --from-beginning --bootstrap-server localhost:9092