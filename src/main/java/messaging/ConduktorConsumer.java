package messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import static messaging.KafkaConfig.MSG_SERVER.CONSUMER;
import static messaging.KafkaConfig.TOPIC_NAME;
import static messaging.KafkaConfig.setupKafkaProps;

public class ConduktorConsumer {

	public enum POLLER {LOOP, HOOK, SCAN}

	private static final Logger LOGGER = LoggerFactory.getLogger(ConduktorConsumer.class.getSimpleName());

	public static void main(String[] args) {

		POLLER poller = POLLER.SCAN;
		setup_CreateSubPoll(poller);
		LOGGER.info("DONE");
	}

	private static void setup_CreateSubPoll(POLLER poller) {

		// setup ConsumerProps
		Properties kafkaProps = setupKafkaProps(CONSUMER);

		// create Consumer
		KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(kafkaProps);

		// add shutdown hooks
		final Thread threadMain = Thread.currentThread();
		Runtime.getRuntime().addShutdownHook(new Thread() {

			// when addShutdownHook is called this triggers exception on kafkaConsumer.poll thread in loop
			public void run() {
				LOGGER.info("INFO: SHUTDOWN DETECTED. Calling consumer.wakeup()...");
				kafkaConsumer.wakeup();

				// join main thread
				try {
					threadMain.join();
				} catch (InterruptedException ex) {
					System.out.println("SHUTDOWN Interrupted: " + ex.getMessage());
				}
			}
		});

		// subscribe
		try {
			kafkaConsumer.subscribe(List.of(TOPIC_NAME));

			// pollData ConsumerRecords
			switch (poller) {

				case LOOP:
					System.out.println("\n\tPOLLER: " + POLLER.LOOP.name());
					pollConsumerRecords_loop(kafkaConsumer);
					break;

				case HOOK:
					System.out.println("\n\tPOLLER: " + POLLER.HOOK.name());
					pollConsumerRecords_hook(kafkaConsumer);
					break;

				case SCAN:
					System.out.println("\n\tPOLLER: " + POLLER.SCAN.name());
					pollConsumerRecords_scan(kafkaConsumer);
					break;

				default:
					System.out.println("\n\t" + "ERROR setup_CreateSubPoll!");

			}

		} finally {
			kafkaConsumer.close();
			LOGGER.info("CONSUMER CLOSED, OFFSETS AUTOMATICALLY COMMITTED.");
		}
	}

	private static void pollConsumerRecords_loop(KafkaConsumer<String, String> kafkaConsumer) {

		while (true) {

			LOGGER.info("POLLING!");

			ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));

			for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {

				LOGGER.info("Key: " + consumerRecord.key() + ", Val: " + consumerRecord.value());
				LOGGER.info("Partition: " + consumerRecord.partition() + ", Offset: " + consumerRecord.offset());
			}
		}
	}

	private static void pollConsumerRecords_hook(KafkaConsumer<String, String> kafkaConsumer) {

		try {
			while (true) {

				LOGGER.info("POLLING!");

				ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));

				for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {

					LOGGER.info("Key: " + consumerRecord.key() + ", Val: " + consumerRecord.value());
					LOGGER.info("Partition: " + consumerRecord.partition() + ", Offset: " + consumerRecord.offset());
				}
			}
		} catch (WakeupException ex) {
			LOGGER.info("SHUTDOWN EXPECTED!");
		} catch (Exception ex) {
			LOGGER.error("ERROR UNEXPECTED! " + ex.getMessage());
		}
	}

	private static void pollConsumerRecords_scan(KafkaConsumer<String, String> kafkaConsumer) {

		try (Scanner scanner = new Scanner(System.in)) {
			while (true) {

				LOGGER.info("POLLING!");

				ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(1000));

				for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {

					LOGGER.info("Key: " + consumerRecord.key() + ", Val: " + consumerRecord.value());
					LOGGER.info("Partition: " + consumerRecord.partition() + ", Offset: " + consumerRecord.offset());
				}

				String input = scanner.nextLine().trim();
				if (input.equalsIgnoreCase("~")) {
					System.out.println("Exiting loop...");
					break;
				}
			}
		} catch (WakeupException ex) {
			LOGGER.info("SHUTDOWN EXPECTED!");
		} catch (Exception ex) {
			LOGGER.error("ERROR UNEXPECTED! " + ex.getMessage());
		}
	}
}
