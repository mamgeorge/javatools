package utils;

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

import java.time.Instant;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

public class KafkaHandler {

	final static String TOPIC_NAME = "quickstart-events";
	final static String BOOTSTRAP_SERVERS = "localhost:9092";
	final static String APACHE_PRFX = "org.apache.kafka.common.serialization.";

	enum Kafkas {PRODUCER, CONSUMER, STREAMS}

	final static Kafkas kafkas = Kafkas.PRODUCER;

	public static void main(String[] args) {
		//
		System.out.println("Subscribed to topic " + TOPIC_NAME);
		switch ( kafkas ) {
			case PRODUCER:
				producer(TOPIC_NAME);
				break;
			case CONSUMER:
				consumer(TOPIC_NAME);
				break;
			case STREAMS:
				streams(TOPIC_NAME);
				break;
		}
		System.out.println("DONE");
	}

	public static void producer(String topicName) {
		//
		Properties props = new Properties();
		props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
		props.put("acks", "all");
		props.put("retries", 0);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", APACHE_PRFX + "StringSerializer");
		props.put("value.serializer", APACHE_PRFX + "StringSerializer");
		//
		Producer<String, String> producer = new KafkaProducer<>(props);
		//
		ProducerRecord<String, String> producerRecord;
		String key;
		String val;
		for ( int ictr = 0; ictr < 10; ictr++ ) {
			key = Integer.toString(ictr);
			val = createValue();
			producerRecord = new ProducerRecord<>(topicName, key, val);
			producer.send(producerRecord);
		}
		System.out.println("Messages Sent!");
		producer.close();
	}

	@NotNull private static String createValue( ) {
		//
		StringBuilder txtLine = new StringBuilder();
		StringBuilder txtRandom = new StringBuilder();
		Random random = new Random();
		char[] chars = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWZYZ".toCharArray();
		double dbl = Math.round(Math.random() * 10000D * 10000D);
		for ( int ictr = 0; ictr < 16; ictr++ ) {
			txtRandom.append(chars[random.nextInt(chars.length)]);
		}
		//
		txtLine.append(Instant.now().toString()).append(" / ");
		txtLine.append((int) dbl);
		txtLine.append(txtRandom);
		return txtLine.toString();
	}

	public static void consumer(String topicName) {
		//
		Properties props = new Properties();
		props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("session.timeout.ms", "30000");
		props.put("key.deserializer", APACHE_PRFX + "StringDeserializer");
		props.put("value.deserializer", APACHE_PRFX + "StringDeserializer");
		//
		KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
		kafkaConsumer.subscribe(Arrays.asList(topicName));
		int ictr = 0;
		String FRMT = "%d: topic: %s, partition: %s, offset: %d, key: %s, value: %s\n";
		String txtRecord;
		while ( true ) {
			ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(100);
			for ( ConsumerRecord<String, String> consumerRecord : consumerRecords ) {
				//
				txtRecord = String.format("Topic: %s, Partition: %s, Value: %s",
					ictr++,
					consumerRecord.topic(),
					consumerRecord.partition(),
					consumerRecord.offset(),
					consumerRecord.key(),
					consumerRecord.value()
				);
				System.out.println(txtRecord);
			}
			kafkaConsumer.close();
		}
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
