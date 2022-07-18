package utils;

import lombok.NonNull;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
	see "kafka_info.md"
	For this java implementation, zookeeper & kafkaServer must be running.
	Note consumer props set "auto.offset.reset" to "earliest", "enable.auto.commit" to "false", and randomizes "group.id".
*/
public class KafkaHandler {

	private final static String[] CLUSTER_NAMES = { "" };
	private final static String[] TOPIC_NAMES = { "quickstart-events" };
	private final static String[] HOSTS = { "localhost" }; // BROKER_1, BROKER_2
	private final static String[] PORTS = { "9092" }; // 9092, 9093
	//
	private final static String GROUP_ID = "test";
	private final static String APACHE_PRFX = "org.apache.kafka.common.serialization.";
	private final static String EOL = "\n";

	private final static boolean testingProducer = false;
	private final static boolean testingConsumer = true;

	public static void main(String[] args) {
		//
		String topicName = TOPIC_NAMES[0];
		//
		System.out.println("Subscribed to topic " + topicName);
		if ( testingProducer ) { producer(topicName); }
		if ( testingConsumer ) { consumer(topicName); }
		//
		System.out.println("DONE");
	}

	public static void producer(String topicName) {
		//
		String host = HOSTS[0];
		String port = PORTS[0];
		String BOOTSTRAP_SERVERS = host + ":" + port;
		Properties properties = getPropertiesProducer(BOOTSTRAP_SERVERS);
		//
		Producer<String, String> kafkaProducer = new KafkaProducer<>(properties);
		ProducerRecord<String, String> producerRecord;
		//
		String txtKey;
		String txtVal;
		for ( int ictr = 0; ictr < 20; ictr++ ) {
			txtKey = Integer.toString(ictr);
			txtVal = getRandomLine();
			producerRecord = new ProducerRecord<>(topicName, txtKey, txtVal);
			kafkaProducer.send(producerRecord);
		}
		System.out.println("Messages Sent!");
		kafkaProducer.close();
	}

	public static void consumer(String topicName) {
		//
		String host = HOSTS[0];
		String port = PORTS[0];
		String BOOTSTRAP_SERVERS = host + ":" + port;
		Properties properties = getPropertiesConsumer(BOOTSTRAP_SERVERS);
		//
		Consumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
		kafkaConsumer.subscribe(Arrays.asList(topicName));
		//
		// kafkaConsumer.seekToBeginning(kafkaConsumer.assignment());
		int ictr = 0;
		String FRMT = "%d: topic: %s, partition: %s, offset: %d, key: %s, value: %s\n";
		String txtRecord;
		while ( true ) {
			ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(100);
			for ( ConsumerRecord<String, String> consumerRecord : consumerRecords ) {
				//
				txtRecord = String.format(FRMT,
					ictr++,
					consumerRecord.topic(),
					consumerRecord.partition(),
					consumerRecord.offset(),
					consumerRecord.key(),
					consumerRecord.value()
				);
				System.out.print(txtRecord);
			}
			// kafkaConsumer.close();
		}
	}

	@NotNull private static Properties getPropertiesProducer(String bootstrapServers) {
		//
		Properties properties = new Properties();
		properties.put("bootstrap.servers", bootstrapServers);
		properties.put("key.serializer", APACHE_PRFX + "StringSerializer");
		properties.put("value.serializer", APACHE_PRFX + "StringSerializer");
		//
		properties.put("acks", "all");
		properties.put("retries", 0);
		properties.put("batch.size", 16384);
		properties.put("linger.ms", 1);
		properties.put("buffer.memory", 33554432);
		//
		return properties;
	}

	@NotNull private static Properties getPropertiesConsumer(String bootstrapServers) {
		//
		Properties properties = new Properties();
		properties.put("bootstrap.servers", bootstrapServers);
		properties.put("key.deserializer", APACHE_PRFX + "StringDeserializer");
		properties.put("value.deserializer", APACHE_PRFX + "StringDeserializer");
		//
		// https://stackoverflow.com/questions/51510250/kafka-from-begining-cli-vs-kafka-java-api
		properties.put("group.id", GROUP_ID + "_" + getRandomString(4));
		properties.put("enable.auto.commit", "false"); // true
		properties.put("auto.offset.reset", "earliest");
		//
		properties.put("auto.commit.interval.ms", "1000");
		properties.put("session.timeout.ms", "30000");
		return properties;
	}

	@NonNull private static String getRandomLine( ) {
		//
		StringBuilder txtLine = new StringBuilder();
		String txtRandom = getRandomString(16);
		//
		txtLine.append(Instant.now().toString()).append(" / ");
		txtLine.append(txtRandom);
		return txtLine.toString();
	}

	@NonNull private static String getRandomString(int num){
		//
		StringBuilder txtRandom = new StringBuilder();
		Random random = new Random();
		char[] chars =
			( "1234567890abcdefghijklmnopqrstuvwxyz" + "ABCDEFGHIJKLMNOPQRSTUVWZYZ" ).toCharArray();
		double dbl = Math.round(Math.random() * 10000D * 10000D);
		for ( int ictr = 0; ictr < num; ictr++ ) {
			txtRandom.append(chars[random.nextInt(chars.length)]);
		}
		return txtRandom.toString();
	}

	@Test void test_getRandomLine( ) {
		//
		String txtLines = "";
		for ( int ictr = 0; ictr < 20; ictr++ ) {
			txtLines += String.format("\t %02d %s \n", ictr + 1, getRandomLine());
		}
		txtLines+=EOL;
		//
		for ( int ictr = 0; ictr < 20; ictr++ ) {
			txtLines += String.format("\t %02d %s \n", ictr + 1, getRandomString(ictr));
		}
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}

	public static void streams(String topicName) {
		//
		String topicOutput = "output-topic";
		StreamsBuilder streamsBuilder = new StreamsBuilder();
		//
		KStream<String, String> textLines = streamsBuilder.stream(topicName);
		//
		KTable<String, Long> kTableWordCounts = textLines
			.flatMapValues(line -> Arrays.asList(line.toLowerCase().split(" ")))
			.groupBy((keyIgnored, word) -> word)
			.count();
		//
		kTableWordCounts.toStream().to(topicOutput, Produced.with(Serdes.String(), Serdes.Long()));
	}
}
