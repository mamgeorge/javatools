package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.jayway.jsonpath.JsonPath;

import java.io.IOException;
import java.util.logging.Logger;

import static com.fasterxml.jackson.core.util.DefaultIndenter.SYS_LF;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static utils.UtilityMain.DLM;

public class JsonTasks {

	public static final Logger LOGGER = Logger.getLogger(JsonTasks.class.getName());

	public static String getJsonPath(String json, String fieldPath) {

		return JsonPath.parse(json).read(fieldPath).toString();
	}

	public static String getJsonNode(String json, String fieldName) {
		//
		String txtLines = "";
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode nodeRoot = objectMapper.readTree(json);
			JsonNode nodePath = nodeRoot.at(fieldName);
			txtLines = nodePath.asText();
		}
		catch (JsonProcessingException ex) {
			System.err.println(ex.getMessage());
		}
		return txtLines;
	}

	public static String getJsonFromObject(Object object) {

		String json = "";
		ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT);
		try { json = objectMapper.writeValueAsString(object); }
		catch (JsonProcessingException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		return json;
	}

	public static Object getObjectFromJson(Class<?> clazz, String json) {
		//
		Object object = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT);
			object = objectMapper.readValue(json, clazz);
		}
		catch (JsonProcessingException ex) {
			System.err.println(ex.getMessage());
		}
		return object;
	}

	public static String formatJson(String json) {

		String txtLines = "";
		try {
			DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter(DLM, SYS_LF);
			DefaultPrettyPrinter dfPrinter = new DefaultPrettyPrinter();
			dfPrinter.indentObjectsWith(indenter);
			dfPrinter.indentArraysWith(indenter);

			ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT);
			ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
			JsonNode jsonNode = objectMapper.readTree(json);
			txtLines = objectWriter.writeValueAsString(jsonNode);
			txtLines = objectMapper.writer(dfPrinter).writeValueAsString(jsonNode);
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		return txtLines;
	}
}
