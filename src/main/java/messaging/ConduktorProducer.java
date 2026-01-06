package messaging;

import lombok.NonNull;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

// io.conduktor.demos.kafka.ProducerDemo

public class ConduktorProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConduktorProducer.class.getSimpleName());
	private static final String KAFKA_HOST_LOCAL = "127.0.0.1:9092";
	private static final String KAFKA_HOST_CLOUD = "cluster.playground.cdkt.io:9092";
	private static final String KAFKA_USER = System.getenv("KAFKA_USER");
	private static final String KAFKA_PASS = System.getenv("KAFKA_PASS");

	private static final String KAFKA_SECURITY = "null"; // protocol SASL_SSL
	private static final String KAFKA_MECHANISM = "PLAIN";
	private static final String KAFKA_LIBRARY = PlainLoginModule.class.getName();
	private static final String KAFKA_SERIALIZER = StringSerializer.class.getName();
	private static final String KAFKA_PARTITIONER = RoundRobinPartitioner.class.getName();
	private static final String KAFKA_BATCHSIZE = "400";

	private static final int KAFKA_WAIT = 500;
	private static final String FRMT = "new metadata: "
			+ "topic....: %s | "
			+ "partition: %s | "
			+ "offset...: %s | "
			+ "timestamp: %s\n";
	private static final String TOPIC_NAME = "demo_java";
	private static final boolean IS_DEPLOYED_SERVER = false;
	private static final Properties kafkaProps = new Properties();

	public static void main(String[] args) {

		runSetupCreateSend();
		LOGGER.info("DONE");
	}

	private static void runSetupCreateSend() {

		setupProducerProps();
		System.out.println(kafkaProps);

		// createProducer
		KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaProps);

		// createProducerRecords
		createProducerRecords_SendBatch(kafkaProducer);

		// close
		kafkaProducer.flush(); // flushes automatically!
		kafkaProducer.close();
	}

	private static void setupProducerProps() {

		// setup localhost properties
		kafkaProps.setProperty("bootstrap.servers", KAFKA_HOST_LOCAL);

		// setup cloudhost properties
		if (IS_DEPLOYED_SERVER) {

			String saslJaasConfig = KAFKA_LIBRARY
					+ " required "
					+ "username=" + KAFKA_USER + " "
					+ "password=" + KAFKA_PASS + ";";

			kafkaProps.setProperty("bootstrap.servers", KAFKA_HOST_CLOUD);
			kafkaProps.setProperty("security.protocol", KAFKA_SECURITY); // SASL_SSL
			kafkaProps.setProperty("sasl.mechanism", KAFKA_MECHANISM);
			kafkaProps.setProperty("sasl.jaas.config", saslJaasConfig);
		}

		// setup Serializers
		kafkaProps.setProperty("key.serializer", KAFKA_SERIALIZER);
		kafkaProps.setProperty("value.serializer", KAFKA_SERIALIZER);

		kafkaProps.setProperty("batch.size", KAFKA_BATCHSIZE); // not for PROD!
		// kafkaProps.setProperty("partitioner.class", KAFKA_PARTITIONER); // not for PROD!
	}

	private static void createProducerRecord_SendOne(KafkaProducer<String, String> kafkaProducer) {

		String dateTime = ISO_DATE_TIME.format(LocalDateTime.now());
		String recordValue = "Howdy World! " + dateTime;
		ProducerRecord<String, String> producerRecord =
				new ProducerRecord<>(TOPIC_NAME, recordValue);

		kafkaProducer.send(producerRecord);
	}

	private static void createProducerRecords_Send(KafkaProducer<String, String> kafkaProducer) {

		ProducerRecord<String, String> producerRecords = null;

		for (int ictr = 0; ictr < 20; ictr++) {

			producerRecords = createProducerRecord(TOPIC_NAME);
			sendCallBack(kafkaProducer, producerRecords);
		}
	}

	private static void createProducerRecords_SendBatch(KafkaProducer<String, String> kafkaProducer) {

		ProducerRecord<String, String> producerRecords = null;

		for (int jctr = 0; jctr < 20; jctr++) {

			for (int ictr = 0; ictr < 20; ictr++) {

				producerRecords = createProducerRecord(TOPIC_NAME);
				sendCallBack(kafkaProducer, producerRecords);
			}

			try {
				Thread.sleep(KAFKA_WAIT);
			} catch (InterruptedException ex) {
				System.out.println("ERROR: " + ex.getMessage());
			}
		}
	}

	private static @NonNull ProducerRecord<String, String> createProducerRecord(String topicName) {

		String dateTime = ISO_DATE_TIME.format(LocalDateTime.now());
		String recordValue = "Hello World! " + dateTime;

		ProducerRecord<String, String> producerRecord =
				new ProducerRecord<>(topicName, recordValue);

		return producerRecord;
	}

	private static void sendCallBack(
			KafkaProducer<String, String> kafkaProducer,
			ProducerRecord<String, String> producerRecord) {

		// send data
		kafkaProducer.send(producerRecord, new Callback() {

			@Override
			public void onCompletion(RecordMetadata recordMetadata, Exception ex) {

				// executes whenever send runs
				if (ex == null) {
					String msg = String.format(FRMT
							, recordMetadata.topic()
							, recordMetadata.partition()
							, recordMetadata.offset()
							, recordMetadata.timestamp()
					);
					System.out.println(msg);
				} else {
					LOGGER.error("ERROR:" + ex.getMessage());
				}
			}
		});
	}

}
