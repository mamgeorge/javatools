package utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.Owner;

import java.util.logging.Logger;
import java.util.Arrays;

// C:/workspace/training/javatools/src/test/java/utils
public class JsonTasksTest {
	//
	public static final String LOGGER_JSONPATH = "com.jayway.jsonpath.internal.path.CompiledPath";

	private static final Logger LOGGER = Logger.getLogger( JsonTasksTest.class.getName( ) );
	private static final String BASE_PATH = "C:/workspace/training/javatools/src/main/java/utils/";

	private static final String jsonFile = BASE_PATH + "owner.json";
	private String json = "";

	@BeforeEach public void init( ) throws Exception {
		//
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger logger = loggerContext.getLogger( LOGGER_JSONPATH );
		logger.setLevel( Level.INFO );
		//
		json = UtilityMain.getFileLines( jsonFile, "" );
	}

	@Test public void getJsonPath() {
		//
		String txtLines = "#### getJsonPath" + "\n";
		String[] fieldNames = { "name", "date", "address.continent", "vehicle[*].model" };
		//
		// txtLines += JsonTasks.getJsonPath(json, "vehicle[*].model");
		StringBuilder stringBuilder = new StringBuilder();
		Arrays.stream(fieldNames).forEach( jpath -> 
			stringBuilder.append("\t" + JsonTasks.getJsonPath(json, jpath) + "\n") );
		txtLines += stringBuilder.toString();
		//
		JsonTasks.LOGGER.info(txtLines);
	}

	@Test public void getJsonNodeObject() {
		//
		String txtLines = "#### getJsonNodeObject" + "\n";
		String jsonValue = JsonTasks.getJsonNodeObject(json);
		txtLines += "value: " + jsonValue + "\n";
		//
		JsonTasks.LOGGER.info(txtLines);
		assertEquals( true, jsonValue.length( ) > 1, "OOPS!" );
	}

	@Test public void getJsonNode() {
		//
		String txtLines = "#### getJsonNode" + "\n";
		String[] fieldName = { "/name", "/date", "/address/continent" };
		//
		String jsonValue = JsonTasks.getJsonNode( json, fieldName[1] );
		txtLines += "fieldName: " + fieldName[1] + ", ";
		txtLines += "value: " + jsonValue + "\n";
		//
		JsonTasks.LOGGER.info(txtLines);
	}
}
