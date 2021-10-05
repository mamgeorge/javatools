package utils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.Arrays;
import java.util.logging.Logger;

public class UtilityMainTest {

	private static final Logger LOGGER = Logger.getLogger(RestTemplateTest.class.getName());
	private static final String PATHFILE_LOCALJSON = "src/test/resources/" + "bodyCatalog.json";
	private static final String ASSERT_MSG = "ASSERT_MSG";

	@Test public void test_showSys() {

		String txtLines = "";
		txtLines = UtilityMain.showSys();
		String[] envr = txtLines.split(",");
		//
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(envr).forEach(str -> stringBuilder.append(str + "\n"));
		System.out.println(stringBuilder);
		System.out.println("envr: " + envr.length);
		Assert.isTrue(envr.length > 60, ASSERT_MSG);
	}

	@Test public void test_showTime() {

		String showTime = UtilityMain.showTime();
		String instantNow = Instant.now().toString();
		//
		System.out.println("showTime: " + showTime);
		System.out.println("instantNow: " + instantNow);
		Assert.isTrue(showTime.length() > 20, ASSERT_MSG);
		Assert.isTrue(instantNow.length() > 28, ASSERT_MSG);
	}

	@Test public void test_showTimes() {

		String showTimes = UtilityMain.showTimes();
		//
		int showTimesLen = showTimes.split("\n").length;
		System.out.println("showTimes: " + showTimes);
		System.out.println("showTimesLen: " + showTimesLen);
		Assert.isTrue(showTimesLen > 4, ASSERT_MSG);
	}

	@Test public void test_getFileLines() {

		String txtLines = "";
		txtLines = UtilityMain.getFileLines("c:/workspace/greetings.txt", "");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		Assert.isTrue(txtLines.length() > 12, ASSERT_MSG);
	}

	@Test public void test_getFileLocal() {

		String txtLines = "";
		txtLines = UtilityMain.getFileLocal(PATHFILE_LOCALJSON, "");
		//
		LOGGER.info(txtLines); // System.out.println(txtLines);
		Assert.isTrue(txtLines.length() > 20, ASSERT_MSG);
	}

	@Test public void test_getJsonValue_fromPath() {

		String txtLine = "";
		String json = UtilityMain.getFileLocal(PATHFILE_LOCALJSON, "");
		String jsonPath = "catalog.book[0].price"; // "/catalog/book/0/price";
		//
		// JsonPath.parse(json).read(fieldPath).toString();
		DocumentContext documentContext = JsonPath.parse(json);
		Object object = documentContext.read(jsonPath);
		txtLine = object.toString();
		//
		LOGGER.info("jsonVal: " + txtLine); // System.out.println(txtLines);
		Assert.isTrue(txtLine.equals("44.95"), ASSERT_MSG);
	}
}
