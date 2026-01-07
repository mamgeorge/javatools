package messaging;

import lombok.NonNull;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Properties;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static messaging.KafkaConfig.KAFKA_WAIT;
import static messaging.KafkaConfig.MSG_SERVER.PRODUCER;
import static messaging.KafkaConfig.TOPIC_NAME;
import static messaging.KafkaConfig.getKafkaCallback;
import static messaging.KafkaConfig.setupKafkaProps;
import static utils.UtilityMain.getRandomLine;

// io.conduktor.demos.kafka.ProducerDemo

public class ConduktorProducer {

	public enum SENDER {ONE, SOME, BATCH, KEYS}
	private static final Logger LOGGER = LoggerFactory.getLogger(ConduktorProducer.class.getSimpleName());

	public static void main(String[] args) {

		SENDER sender = SENDER.ONE;
		setup_CreateSend(sender);
		LOGGER.info("DONE");
	}

	private static void setup_CreateSend(SENDER sender) {

		// setup ProducerProps
		Properties kafkaProps = setupKafkaProps(PRODUCER);

		// create Producer
		KafkaProducer<String, String> kafkaProducer = new KafkaProducer<>(kafkaProps);

		// create ProducerRecords
		switch (sender) {

			case ONE:
				System.out.println("\n\tSENDER: " + SENDER.ONE.name());
				createProducerRecord_sendOne(kafkaProducer);
				break;
			case SOME:
				System.out.println("\n\tSENDER: " + SENDER.SOME.name());
				createProducerRecords_sendSome(kafkaProducer);
				break;
			case BATCH:
				System.out.println("\n\tSENDER: " + SENDER.BATCH.name());
				createProducerRecords_sendBatch(kafkaProducer);
				break;
			case KEYS:
				System.out.println("\n\tSENDER: " + SENDER.KEYS.name());
				createProducerRecords_sendKeys(kafkaProducer);
				break;
			default:
				System.out.println("\n\t" + "ERROR setup_CreateSend!");
		}

		// close Producer
		kafkaProducer.flush(); // flushes automatically!
		kafkaProducer.close();
	}

	private static void createProducerRecord_sendOne(KafkaProducer<String, String> kafkaProducer) {

		String dateTime = ISO_DATE_TIME.format(LocalDateTime.now());
		String recordKey = "MLG";
		String recordVal = "Howdy World! " + dateTime;
		ProducerRecord<String, String> producerRecord =
				new ProducerRecord<>(TOPIC_NAME, recordKey, recordVal);

		kafkaProducer.send(producerRecord);
	}

	private static void createProducerRecords_sendSome(KafkaProducer<String, String> kafkaProducer) {

		ProducerRecord<String, String> producerRecords = null;

		for (int ictr = 0; ictr < 20; ictr++) {

			producerRecords = createProducerRecord(TOPIC_NAME, "##");
			sendCallBack(kafkaProducer, producerRecords, null);
		}
	}

	private static void createProducerRecords_sendBatch(KafkaProducer<String, String> kafkaProducer) {

		ProducerRecord<String, String> producerRecords = null;
		String tmp = "";

		for (int jctr = 0; jctr < 20; jctr++) {

			for (int ictr = 0; ictr < 20; ictr++) {

				tmp = "##" + jctr + " | " + ictr;
				producerRecords = createProducerRecord(TOPIC_NAME, tmp);
				sendCallBack(kafkaProducer, producerRecords, null);
			}

			try {
				Thread.sleep(KAFKA_WAIT);
			} catch (InterruptedException ex) {
				System.out.println("ERROR: " + ex.getMessage());
			}
		}
	}

	private static void createProducerRecords_sendKeys(KafkaProducer<String, String> kafkaProducer) {

		ProducerRecord<String, String> producerRecords = null;
		String key = "", val = "";

		for (int jctr = 0; jctr < 2; jctr++) {
			for (int ictr = 0; ictr < 10; ictr++) {

				key = "id: " + ictr;
				val = "val: " + ictr + getRandomLine(10);
				producerRecords = createProducerRecord(TOPIC_NAME, key, val);
				sendCallBack(kafkaProducer, producerRecords, key);
			}
		}
	}

	private static @NonNull ProducerRecord<String, String> createProducerRecord(
			String topicName, String val) {

		String dateTime = ISO_DATE_TIME.format(LocalDateTime.now());
		String recordValue = val + "Hello World! " + dateTime;

		ProducerRecord<String, String> producerRecord =
				new ProducerRecord<>(topicName, recordValue);

		return producerRecord;
	}

	private static @NonNull ProducerRecord<String, String> createProducerRecord(
			String topicName, String key, String val) {

		ProducerRecord<String, String> producerRecord =
				new ProducerRecord<>(topicName, key, val);

		return producerRecord;
	}

	private static void sendCallBack(
			KafkaProducer<String, String> kafkaProducer,
			ProducerRecord<String, String> producerRecord, String key) {

		// send data
		kafkaProducer.send(producerRecord, getKafkaCallback(key));
	}
}
