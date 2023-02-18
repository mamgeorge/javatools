package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import samples.BooksCatalog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;
import static utils.UtilityMainTest.PATHFILE_LOCAL;

// C:/workspace/training/javatools/src/test/java/utils
public class UtilityFormatsTest {
	//
	private static final Logger LOGGER = Logger.getLogger(UtilityFormatsTest.class.getName());
	private static final String JSONFILE = PATHFILE_LOCAL + "booksCatalog.json";
	private static final String ASSERT_MSG = "ASSERT_MSG";
	private String json = "";

	@BeforeEach void init( ) {
		//
		json = UtilityMain.getFileLocal(JSONFILE);
		System.out.println(json.substring(0, 20));
	}

	@Test void getJsonPath( ) {
		//
		String txtLines = "#### getJsonPath" + EOL;
		String[] fieldNames = { "id", "author", "price", "title" }; // catalog.book[*].
		int aint = 1;
		//
		// txtLines += JsonTasks.getJsonPath(json, "vehicle[*].model");
		StringBuilder stringBuilder = new StringBuilder();
		for ( String jpath : fieldNames ) {
			stringBuilder
				.append(String.format("\t%s",
					JsonTasks.getJsonPath(json, "catalog.book[" + aint + "]." + jpath)));
		}
		txtLines += stringBuilder.toString();
		System.out.println(txtLines);
		assertTrue(txtLines.contains("Midnight"));
	}

	@Test void getJsonNodeObject( ) {
		//
		String txtLines = "#### getJsonNodeObject" + EOL;
		BooksCatalog booksCatalog = (BooksCatalog) JsonTasks.getJsonNodeObject(BooksCatalog.class, json);
		String title = booksCatalog.catalog.book.get(0).title;
		txtLines += "title: " + title + EOL;
		//
		System.out.println(txtLines);
		assertTrue(title.contains("Developers"));
	}

	@Test void getJsonNode( ) {
		//
		String txtLines = "#### getJsonNode" + EOL;
		String[] fieldNames = { "id", "author", "price", "title" };
		int aint = 3;
		//
		String jsonValue = JsonTasks.getJsonNode(json, "/catalog/book/" + aint + "/" + fieldNames[aint]);
		txtLines += "fieldName: " + fieldNames[aint] + ", value: " + jsonValue + EOL;
		//
		System.out.println(txtLines);
		assertTrue(jsonValue.contains("Oberon"));
	}

	@Test void test_objectMapper( ) {
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
		}
		catch (JsonProcessingException ex) {
			LOGGER.severe(ex.getMessage());
		}
		//
		System.out.println("jsonNodeAt.asText(): " + txtLine);
		assertTrue(txtLine.equals("Gambardella , Matthew"), ASSERT_MSG);
	}

	@Test void test_getJsonValue_fromPath( ) {
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
		assertTrue(txtLine.equals("44.95"), ASSERT_MSG);
	}

	@Test void getXmlNodeFromXPath( ) {
		//
		String xml = UtilityMain.getFileLocal(PATHFILE_LOCAL + "booksCatalog.xml");
		String xmlPath = "/catalog/book[5]/price";
		String txtLines = UtilityFormats.getXmlNodeFromXPath(xml, xmlPath);
		System.out.println(txtLines);
		assertTrue(txtLines.contains("5.95"), ASSERT_MSG);
	}

	@Test void formatXml( ) {
		//
		String xml = "<a><b><c>Boo</c></b></a>";
		String txtLines = UtilityFormats.formatXml(xml);
		System.out.println(txtLines);
		assertTrue(txtLines.split("\n").length >= 5, ASSERT_MSG);
	}

	@Test void transformXslt( ) {
		//
		String filename_XML = PATHFILE_LOCAL + "booksCatalog" + ".xml";
		String filename_XSL = PATHFILE_LOCAL + "booksXml2Html" + ".xslt";
		String xml = UtilityMain.getFileLocal(filename_XML);
		String xsl = UtilityMain.getFileLocal(filename_XSL);
		//
		String txtLines = UtilityFormats.transformXslt(xml, xsl);
		//
		System.out.println(txtLines);
		try { Files.write(Paths.get(PATHFILE_LOCAL + "booksCatalog.html"), txtLines.getBytes(UTF_8)); }
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertTrue(txtLines.length() > 20, ASSERT_MSG);
	}

	@Test void parseYaml2JsonNode( ) {
	}

	@Test void parseJsonList2List( ) {
	}
}
