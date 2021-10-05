package utils;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.util.logging.Logger;

public class UtilityMainTest {

	private static final Logger LOGGER = Logger.getLogger(RestTemplateTest.class.getName());
	private static final String PATHFILE_LOCALJSON = "src/test/resources/" + "bodyCatalog.json";
	private static final String ASSERT_MSG = "ASSERT_MSG";

	@Test public void test_getFileLocal() {

		String txtLines = "";
		txtLines = UtilityMain.getFileLocal(PATHFILE_LOCALJSON, "");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		Assert.isTrue(txtLines.length() > 20, ASSERT_MSG);
	}
}
