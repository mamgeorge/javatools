package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import java.util.logging.Logger;

public class JsonTasks {
	//
	public static final Logger LOGGER = Logger.getLogger(JsonTasks.class.getName());

	public static void main(String[] args) {
		LOGGER.info("main");
		System.out.println("DONE");
	}

	public static String getJsonPath(String json, String fieldPath) {
		//
		return JsonPath.parse(json).read(fieldPath).toString();
	}

	public static Object getJsonNodeObject(Class clazz, String json) {
		//
		Object object = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			object = objectMapper.readValue(json, clazz);
		}
		catch (JsonProcessingException ex) {
			System.err.println(ex.getMessage());
		}
		return object;
	}

	public static String getJsonNode(String json, String fieldName) {
		//
		String txtLines = "";
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode nodeRoot = objectMapper.readTree(json);
			JsonNode nodePath = nodeRoot.at(fieldName);
			txtLines = nodePath.textValue();
		}
		catch (JsonProcessingException ex) {
			System.err.println(ex.getMessage());
		}
		return txtLines;
	}
}
