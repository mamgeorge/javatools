package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import samples.BooksCatalog;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;

// C:/workspace/training/javatools/src/test/java/utils
public class JsonTasksTest {
	//
	private static final Logger LOGGER = Logger.getLogger(JsonTasksTest.class.getName());
	private static final String BASE_PATH = "src/test/resources/";
	private static final String JSONFILE = BASE_PATH + "booksCatalog.json";
	private static final String PATHFILE_LOCAL = "src/test/resources/";
	private static final String ASSERT_MSG = "ASSERT_MSG";
	private String json = "";

	@BeforeEach void init() {
		//
		json = UtilityMain.getFileLocal(JSONFILE);
		System.out.println(json.substring(0, 20));
	}

	@Test void getJsonPath() {
		//
		String txtLines = "#### getJsonPath" + EOL;
		String[] fieldNames = {"id", "author", "price", "title"}; // catalog.book[*].
		int aint = 1;
		//
		// txtLines += JsonTasks.getJsonPath(json, "vehicle[*].model");
		StringBuilder stringBuilder = new StringBuilder();
		for (String jpath : fieldNames) {
			stringBuilder
					.append(String.format("\t%s",
							JsonTasks.getJsonPath(json, "catalog.book[" + aint + "]." + jpath)));
		}
		txtLines += stringBuilder.toString();
		System.out.println(txtLines);
		assertTrue(txtLines.contains("Midnight"));
	}

	@Test void getJsonNodeObject() {
		//
		String txtLines = "#### getJsonNodeObject" + EOL;
		BooksCatalog booksCatalog = (BooksCatalog) JsonTasks.getJsonNodeObject(BooksCatalog.class, json);
		String title = booksCatalog.catalog.book.get(0).title;
		txtLines += "title: " + title + EOL;
		//
		System.out.println(txtLines);
		assertTrue(title.contains("Developers"));
	}

	@Test void getJsonNode() {
		//
		String txtLines = "#### getJsonNode" + EOL;
		String[] fieldNames = {"id", "author", "price", "title"};
		int aint = 3;
		//
		String jsonValue = JsonTasks.getJsonNode(json, "/catalog/book/" + aint + "/" + fieldNames[aint]);
		txtLines += "fieldName: " + fieldNames[aint] + ", value: " + jsonValue + EOL;
		//
		System.out.println(txtLines);
		assertTrue(jsonValue.contains("Oberon"));
	}

	@Test void test_objectMapper() {
		//
		String txtLine = "";
		String JSON_PATH = "/catalog/book/0/author";
		String PATHFILE_LOCALJSON = PATHFILE_LOCAL + "booksCatalog.json";
		String body = UtilityMain.getFileLocal(PATHFILE_LOCALJSON);
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNodeRoot = objectMapper.readTree(body);
			JsonNode jsonNodeAt = jsonNodeRoot.at(JSON_PATH);
			txtLine = jsonNodeAt.asText();
			// txtLine = jsonNodeRoot.at(JsonPointer.compile(jsonPath)).asText();
		} catch (JsonProcessingException ex) {
			LOGGER.severe(ex.getMessage());
		}
		//
		System.out.println("jsonNodeAt.asText(): " + txtLine);
		Assert.isTrue(txtLine.equals("Gambardella , Matthew"), ASSERT_MSG);
	}

	@Test void test_getJsonValue_fromPath() {
		//
		String json = UtilityMain.getFileLocal(PATHFILE_LOCAL + "booksCatalog.json");
		String jsonPath = "catalog.book[0].price"; // "/catalog/book/0/price";
		//
		// JsonPath.parse(json).read(fieldPath).toString();
		DocumentContext documentContext = JsonPath.parse(json);
		Object object = documentContext.read(jsonPath);
		String txtLine = object.toString();
		//
		LOGGER.info("jsonVal: " + txtLine); // System.out.println(txtLines);
		Assert.isTrue(txtLine.equals("44.95"), ASSERT_MSG);
	}

	@Test void getXmlNode() {
		//
		String xml = UtilityMain.getFileLocal(PATHFILE_LOCAL + "booksCatalog.xml");
		String xmlPath = "/catalog/book[5]/price";
		String txtLines = UtilityMain.getXmlNode(xml, xmlPath);
		System.out.println(txtLines);
		Assert.isTrue(txtLines.contains("5.95"), ASSERT_MSG);
	}

	@Test void formatXml() {
		//
		String xml = "<a><b><c>Boo</c></b></a>";
		String txtLines = UtilityMain.formatXml(xml);
		System.out.println(txtLines);
		Assert.isTrue(txtLines.split("\n").length >= 5, ASSERT_MSG);
	}

	@Test void formatJson() {
		//
		String json = "{\"a\":\"aleph\",\"b\":\"beth\",\"g\":\"gimmel\"}";
		String txtLines = "";
		try {
			// JACKSON
			ObjectMapper objectMapper = new ObjectMapper();
			Object object = objectMapper.readValue(json, Object.class);
			txtLines += objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object) + EOL;
		} catch (Exception ex) {
			LOGGER.info(ex.getMessage());
		}
		//
		// GSON
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonElement jsonElement = JsonParser.parseString(json);
		txtLines += gson.toJson(jsonElement);
		//
		System.out.println(txtLines);
		Assert.isTrue(txtLines.split("\n").length >= 5, ASSERT_MSG);
	}

	@Test void parseYaml2JsonNode() {
	}

	@Test void parseJsonList2List() {
	}
}
