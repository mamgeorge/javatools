package messaging;

import lombok.NonNull;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Random;

import static messaging.KafkaZookeeper.getRandomLine;
import static messaging.KafkaZookeeper.getRandomString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static utils.UtilityMain.EOL;


class KafkaTests {

	@Test
	void test_getRandomLine( ) {
		//
		String txtLines = "";
		for ( int ictr = 0; ictr < 20; ictr++ ) {
			txtLines += String.format("\t %02d %s \n", ictr + 1, getRandomLine());
		}
		txtLines += EOL;
		//
		for ( int ictr = 0; ictr < 20; ictr++ ) {
			txtLines += String.format("\t %02d %s \n", ictr + 1, getRandomString(ictr));
		}
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}
}
