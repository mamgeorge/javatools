package messaging;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaConfig {

	public enum MSG_SERVER {PRODUCER, CONSUMER}

	public static final int KAFKA_WAIT = 500;
	public static final String TOPIC_NAME = "demo_java";
	public static final String GROUP_NAME = "demo_group_java";
	public static final String AUTO_OFFSET_RESET = "earliest"; // "none/earliest/latest"

	public static final String FRMT = "new metadata: "
			+ "topic....: %s | "
			+ "key......: %s | "
			+ "partition: %s | "
			+ "offset...: %s | "
			+ "timestamp: %s\n";

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfig.class.getSimpleName());
	private static final String KAFKA_HOST_LOCAL = "127.0.0.1:9092";
	private static final String KAFKA_HOST_CLOUD = "cluster.playground.cdkt.io:9092";
	private static final String KAFKA_USER = System.getenv("KAFKA_USER");
	private static final String KAFKA_PASS = System.getenv("KAFKA_PASS");
	private static final String KAFKA_SECURITY = "null"; // protocol SASL_SSL
	private static final String KAFKA_MECHANISM = "PLAIN";
	private static final String KAFKA_LIBRARY = PlainLoginModule.class.getName();
	private static final String KAFKA_STRING_SERIALIZER = StringSerializer.class.getName();
	private static final String KAFKA_STRING_DESERIALIZER = StringDeserializer.class.getName();
	private static final String KAFKA_PARTITIONER = RoundRobinPartitioner.class.getName();
	private static final String KAFKA_BATCHSIZE = "400";
	private static final boolean IS_DEPLOYED_SERVER = false;

	public static Properties setupKafkaProps(MSG_SERVER msgServer) {

		// setup localhost properties
		Properties kafkaProps = new Properties();
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
		switch(msgServer) {

			case PRODUCER:
				System.out.println("\n\tMSG_SERVER: " + MSG_SERVER.PRODUCER.name());
				kafkaProps.setProperty("key.serializer", KAFKA_STRING_SERIALIZER);
				kafkaProps.setProperty("value.serializer", KAFKA_STRING_SERIALIZER);
				break;

			case CONSUMER:
				System.out.println("\n\tMSG_SERVER: " + MSG_SERVER.CONSUMER.name());
				kafkaProps.setProperty("key.deserializer", KAFKA_STRING_DESERIALIZER);
				kafkaProps.setProperty("value.deserializer", KAFKA_STRING_DESERIALIZER);

				kafkaProps.setProperty("group.id", GROUP_NAME);
				kafkaProps.setProperty("auto.offset.reset", AUTO_OFFSET_RESET);
				break;

			default:
				System.out.println("\n\t" + "ERROR setupKafkaProps!");
		}

		// additional values; not for PROD!
		kafkaProps.setProperty("batch.size", KAFKA_BATCHSIZE);
		System.out.println("Not using partitioner.class: " + KAFKA_PARTITIONER);

		return kafkaProps;
	}

	public static Callback getKafkaCallback(String key) {

		return new Callback() {

			@Override
			public void onCompletion(RecordMetadata recordMetadata, Exception ex) {

				// executes whenever send runs
				if (ex == null) {
					String msg = String.format(FRMT
							, recordMetadata.topic()
							, key
							, recordMetadata.partition()
							, recordMetadata.offset()
							, recordMetadata.timestamp()
					);
					System.out.println(msg);
				} else {
					LOGGER.error("ERROR:" + ex.getMessage());
				}
			}
		};
	}
}
