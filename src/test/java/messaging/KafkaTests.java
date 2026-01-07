package messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static utils.UtilityMain.getRandomLine;
import static utils.UtilityMain.getRandomString;


class KafkaTests {

	@Test
	void test_getRandomLine() {
		//
		String txtLines = "";
		for (int ictr = 0; ictr < 20; ictr++) {
			txtLines += String.format("\t %02d %s \n", ictr + 1, getRandomLine(10));
		}
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}

	@Test
	void test_getRandomString() {

		String txtLines = "";
		for (int ictr = 0; ictr < 20; ictr++) {
			txtLines += String.format("\t %02d %s \n", ictr + 1, getRandomString(ictr));
		}
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}
}
