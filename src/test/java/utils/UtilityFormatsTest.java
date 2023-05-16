package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Test;
import samples.BooksCatalog;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;
import static utils.UtilityMain.getFileLocal;
import static utils.UtilityMainTest.PATHFILE_LOCAL;

// C:/workspace/training/javatools/src/test/java/utils
public class UtilityFormatsTest {

	private static final Logger LOGGER = Logger.getLogger(UtilityFormatsTest.class.getName());
	private static final String JSONFILE = PATHFILE_LOCAL + "booksCatalog.json";
	private static final String ASSERT_MSG = "ASSERT_MSG";
	private String json = "";

	// json
	@Test void getJsonPath( ) {

		json = getFileLocal(JSONFILE);
		String txtLines = "#### getJsonPath" + EOL;
		String[] fieldNames = { "id", "author", "price", "title" }; // catalog.book[*].
		int aint = 1;

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

		json = getFileLocal(JSONFILE);
		String txtLines = "#### getJsonNodeObject" + EOL;
		BooksCatalog booksCatalog = (BooksCatalog) JsonTasks.getJsonNodeObject(BooksCatalog.class, json);
		String title = booksCatalog.catalog.book.get(0).title;
		txtLines += "title: " + title + EOL;

		System.out.println(txtLines);
		assertTrue(title.contains("Developers"));
	}

	@Test void getJsonNode( ) {

		json = getFileLocal(JSONFILE);
		String txtLines = "#### getJsonNode" + EOL;
		String[] fieldNames = { "id", "author", "price", "title" };
		int aint = 3;

		String jsonValue = JsonTasks.getJsonNode(json, "/catalog/book/" + aint + "/" + fieldNames[aint]);
		txtLines += "fieldName: " + fieldNames[aint] + ", value: " + jsonValue + EOL;

		System.out.println(txtLines);
		assertTrue(jsonValue.contains("Oberon"));
	}

	@Test void test_objectMapper( ) {

		String txtLine = "";
		String JSON_PATH = "/catalog/book/0/author";
		String PATHFILE_LOCALJSON = PATHFILE_LOCAL + "booksCatalog.json";
		String body = getFileLocal(PATHFILE_LOCALJSON);
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
		System.out.println("jsonNodeAt.asText(): " + txtLine);
		assertTrue(txtLine.equals("Gambardella , Matthew"), ASSERT_MSG);
	}

	@Test void getJsonValue_fromPath( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "booksCatalog.json");
		String jsonPath = "catalog.book[0].price"; // "/catalog/book/0/price";

		// JsonPath.parse(json).read(fieldPath).toString();
		DocumentContext documentContext = JsonPath.parse(json);
		Object object = documentContext.read(jsonPath);
		String txtLine = object.toString();

		LOGGER.info("jsonVal: " + txtLine); // System.out.println(txtLines);
		assertTrue(txtLine.equals("44.95"), ASSERT_MSG);
	}

	@Test void parseJsonList2List( ) {
	}

	// xml
	@Test void getXmlNodeFromXPath( ) {

		String xml = getFileLocal(PATHFILE_LOCAL + "booksCatalog.xml");
		String xmlPath = "/catalog/book[5]/price";
		String txtLines = UtilityFormats.getXmlNodeFromXPath(xml, xmlPath);
		System.out.println(txtLines);
		assertTrue(txtLines.contains("5.95"), ASSERT_MSG);
	}

	@Test void formatXml( ) {

		String xml = "<a><b><c>Boo</c></b></a>";
		String txtLines = UtilityFormats.formatXml(xml);
		System.out.println(txtLines);
		assertTrue(txtLines.split("\n").length >= 5, ASSERT_MSG);
	}

	@Test void transformXsl( ) {

		String filenameXML = PATHFILE_LOCAL + "booksCatalog.xml";
		String filenameXSL = PATHFILE_LOCAL + "booksXml2Html.xsl";
		String xml = getFileLocal(filenameXML);
		String xsl = getFileLocal(filenameXSL);

		String html = UtilityFormats.transformXslt(xml, xsl);

		System.out.println(html);
		try { Files.write(Paths.get(PATHFILE_LOCAL + "booksCatalog.html"), html.getBytes(UTF_8)); }
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(html);
	}

	@Test void transformCsv2Html( ) {

		String html = "";
		String PATHFILE_REMOTE = "C:/workspace/";
		String filePath = PATHFILE_LOCAL + "battles.csv";
		try {
			Reader reader = Files.newBufferedReader(Path.of(filePath));
			CSVReader csvReader = new CSVReader(reader);
			List<String[]> list = csvReader.readAll();

			StringBuilder stringBuilder = new StringBuilder(EOL);
			list.forEach(strings -> {
				String classy = "";
				if ( strings[1].equals("") ) {
					stringBuilder.append("<tr><td colspan = \"4\"> </td></tr>" + EOL);
				} else {
					int year = Integer.parseInt(strings[0]);
					if ( year >= -3300 && year < -2050 ) { classy = "red"; }
					if ( year >= -2050 && year < -1406) { classy = "ora"; }
					if ( year >= -1406 && year < -1050) { classy = "yel"; }
					if ( year >= -1050 && year < -715) { classy = "grn"; }
					if ( year >= -715 && year < -626) { classy = "blu"; }
					if ( year >= -626 && year < -586) { classy = "ind"; }
					if ( year >= -586 && year < -0) { classy = "vio"; }
					stringBuilder.append("<tr class = \"" + classy + "\">");
					Arrays.stream(strings).forEach(string -> stringBuilder.append("<td>" + string + "</td>"));
					stringBuilder.append("</tr>" + EOL);
				}
			});
			html = getFileLocal(PATHFILE_LOCAL + "header.html");
			html = html.replaceAll("battleData", stringBuilder.toString());
		}
		catch (IOException | CsvException ex) { System.out.println("ERROR: " + ex.getMessage()); }

		System.out.println(html);
		try { Files.write(Paths.get(PATHFILE_REMOTE + "battles.html"), html.getBytes(UTF_8)); }
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(html);
	}

	// yaml
	@Test void parseYaml2JsonNode( ) {
	}

}
