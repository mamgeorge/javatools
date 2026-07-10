package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import objects.BooksCatalog;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.JsonTasks.getJsonFromObject;
import static utils.UtilityMain.EOL;
import static utils.UtilityMain.getFileLocal;
import static utils.UtilityMainTest.ASSERT_MSG;
import static utils.UtilityMainTest.PATHFILE_LOCAL;

// C:/workspace/training/javatools/src/test/java/utils
class JsonTaskTests {

	private static final Logger LOGGER = Logger.getLogger(JsonTaskTests.class.getName());

	@Test void testMapping() {

		Person person = Person.createPerson();
		Student student = Student.createStudent();
		System.out.println("person: " + getJsonFromObject(person));
		System.out.println("student: " + getJsonFromObject(student));

		// BeanUtils
		BeanUtils.copyProperties(person, student);
		System.out.println("studentBU: " + getJsonFromObject(student));

		// ObjectMapper
		ObjectMapper objectMapper = new ObjectMapper();
		Student studentOM = Student.createStudent();
		try { objectMapper.updateValue(studentOM, person); }
		catch (JsonMappingException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		System.out.println("studentOM: " + getJsonFromObject(studentOM));

		// ModelMapper
		ModelMapper modelMapper = new ModelMapper();
		Student studentMM = Student.createStudent();
		modelMapper.map(person, studentMM);
		System.out.println("studentMM: " + getJsonFromObject(studentMM));

		assertEquals(student.getName(),person.getName());
		assertEquals(studentOM.getName(),person.getName());
		assertEquals(studentMM.getName(),person.getName());
	}

	@Test void getJsonSchema_validate( ) { // not working

		try {
			ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT);
			InputStream inputStreamJson = new FileInputStream(PATHFILE_LOCAL + "structured/booksCatalog.json");
			InputStream inputStreamSchema = new FileInputStream(PATHFILE_LOCAL + "structured/books_schema.json");

			SpecVersion.VersionFlag versionFlag = SpecVersion.VersionFlag.V202012;
			JsonSchemaFactory JSF = JsonSchemaFactory.getInstance(versionFlag);
			JsonSchema jsonSchema = JSF.getSchema(inputStreamSchema);
			JsonNode jsonNodes = objectMapper.readTree(inputStreamJson);

			Set<ValidationMessage> errors = jsonSchema.validate(jsonNodes);
			if(errors.isEmpty()) { System.out.println("No Errors!"); } else {
				errors.forEach(item -> System.out.println("ERROR: " + item.getMessage()));
				assertFalse(errors.isEmpty());
			}
		}
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
	}

	@Test void getJsonNode_at( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "/catalog/book/0/price"; // "/catalog/book/0/price";

		String txtLine = "";
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode jsonNodeRoot = objectMapper.readTree(json);
			JsonNode jsonNodeAt = jsonNodeRoot.at(jsonPath);
			txtLine = jsonNodeAt.asText();
		}
		catch (JsonProcessingException ex) {
			LOGGER.severe(ex.getMessage());
		}

		System.out.println("jsonVal: " + txtLine);
		assertEquals("44.95", txtLine);
	}

	@Test void getJsonNode_atmethod( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "/catalog/book/0/price"; // "/catalog/book/0/price";

		String txtLine = JsonTasks.getJsonNode(json, jsonPath);

		System.out.println("jsonVal: " + txtLine);
		assertEquals("44.95", txtLine);
	}

	@Test void getJsonPath_One( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "$.catalog.book[0].price"; // catalog.book[0].price = /catalog/book/0/price

		// JsonPath.parse(json).read(jsonPath).toString();
		DocumentContext documentContext = JsonPath.parse(json);
		Object object = documentContext.read(jsonPath);
		String txtLine = object.toString();

		System.out.println("jsonVal: " + txtLine);
		assertEquals("44.95", txtLine);
	}

	@Test void getJsonPath_Any0( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "$.catalog.book[*].price"; // catalog.book[0].price = /catalog/book/0/price

		// JsonPath.parse(json).read(jsonPath).toString();
		DocumentContext documentContext = JsonPath.parse(json);
		Object object = documentContext.read(jsonPath);
		String txtLine = object.toString();

		System.out.println("jsonVal: " + txtLine);
		assertEquals("[44.95,5.95,5.95,5.95,5.95,4.95,4.95,4.95,6.95,36.95,36.95,49.95]", txtLine);
	}

	@Test void getJsonPath_Any1( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "$..price"; // catalog.book[0].price = /catalog/book/0/price

		// JsonPath.parse(json).read(jsonPath).toString();
		DocumentContext documentContext = JsonPath.parse(json);
		Object object = documentContext.read(jsonPath);
		String txtLine = object.toString();

		System.out.println("jsonVal: " + txtLine);
		assertEquals("[44.95,5.95,5.95,5.95,5.95,4.95,4.95,4.95,6.95,36.95,36.95,49.95]", txtLine);
	}

	@Test void getJsonPath_branch( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "$.catalog.book[0]";

		Object object = JsonPath.read(json,jsonPath);

		String branch = "";
		ObjectMapper objectMapper = new ObjectMapper().enable(INDENT_OUTPUT);
		try { branch = objectMapper.writeValueAsString(object); }
		catch (JsonProcessingException ex) { System.out.println("ERROR: " + ex.getMessage()); }

		System.out.println("branch: " + branch);
		assertFalse(branch.isEmpty());
	}

	@Test void getJsonPath_expr( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "$..book[?(@.genre contains 'Computer')].id";

		Object object = JsonPath.read(json, jsonPath);
		String txtLine = object.toString();

		System.out.println("jsonVal: " + txtLine);
		assertFalse(txtLine.isEmpty());
	}

	@Test void getJsonPath_empty( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "$..book[?(@.genre empty false)].id";

		Object object = JsonPath.read(json, jsonPath);
		String txtLine = object.toString();

		System.out.println("jsonVal: " + txtLine);
		assertFalse(txtLine.isEmpty());
	}

	@Test void getJsonPath_regx( ) {

		// regx reads first two letters "ra" case insensitive
		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String jsonPath = "$..book[?(@.author =~ /ra.*/i)].id";

		Object object = JsonPath.read(json, jsonPath);
		String txtLine = object.toString();

		System.out.println("jsonVal: " + txtLine);
		assertFalse(txtLine.isEmpty());
	}

	@Test void getObjectFromJson( ) {

		String json = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.json");
		String txtLines = "#### getJsonNodeObject" + EOL;
		BooksCatalog booksCatalog = (BooksCatalog) JsonTasks.getObjectFromJson(BooksCatalog.class, json);
		String title = booksCatalog.catalog.book.get(0).title;
		txtLines += "title: " + title + EOL;

		System.out.println(txtLines);
		assertTrue(title.contains("Developers"));
	}

	// xml
	@Test void getXmlNodeFromXPath( ) {

		String xml = getFileLocal(PATHFILE_LOCAL + "structured/booksCatalog.xml");
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

		String filenameXML = PATHFILE_LOCAL + "structured/booksCatalog.xml";
		String filenameXSL = PATHFILE_LOCAL + "structured/booksXml2Html.xsl";
		String xml = getFileLocal(filenameXML);
		String xsl = getFileLocal(filenameXSL);

		String html = UtilityFormats.transformXslt(xml, xsl);

		System.out.println(html);
		try { Files.writeString(Paths.get(PATHFILE_LOCAL + "structured/booksCatalog.html"), html); }
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(html);
	}

	@Test void transformCsv2Html( ) {

		String html = "";
		String PATHFILE_REMOTE = "C:/workspace/";
		String filePath = PATHFILE_LOCAL + "structured/battles.csv";
		try {
			Reader reader = Files.newBufferedReader(Path.of(filePath));
			CSVReader csvReader = new CSVReader(reader);
			List<String[]> list = csvReader.readAll();

			StringBuilder stringBuilder = new StringBuilder(EOL);
			list.forEach(strings -> {
				String classy = "";
				if ( strings[1].isEmpty() ) {
					stringBuilder.append("<tr><td colspan = \"4\"> </td></tr>" + EOL);
				} else {
					int year = Integer.parseInt(strings[0]);
					if ( year >= -3300 && year < -2050 ) { classy = "red"; }
					if ( year >= -2050 && year < -1406 ) { classy = "ora"; }
					if ( year >= -1406 && year < -1050 ) { classy = "yel"; }
					if ( year >= -1050 && year < -715 ) { classy = "grn"; }
					if ( year >= -715 && year < -626 ) { classy = "blu"; }
					if ( year >= -626 && year < -586 ) { classy = "ind"; }
					if ( year >= -586 && year < -0 ) { classy = "vio"; }
					stringBuilder.append("<tr class = \"").append(classy).append("\">");
					Arrays.stream(strings)
						.forEach(string -> stringBuilder.append("<td>").append(string).append("</td>"));
					stringBuilder.append("</tr>" + EOL);
				}
			});
			html = getFileLocal(PATHFILE_LOCAL + "structured/header.html");
			html = html.replaceAll("battleData", stringBuilder.toString());
		
			csvReader.close();
			reader.close();	
		}
		catch (IOException | CsvException ex) { System.out.println("ERROR: " + ex.getMessage()); }

		System.out.println(html);
		try { Files.writeString(Paths.get(PATHFILE_REMOTE + "battles.html"), html); }
		catch (IOException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		assertNotNull(html);
	}
}

@Data class Person {

	private String name;
	private String address;
	private String phone;
	private Date birthdate;
	private List<String> family;

	public static Person createPerson(){

		Person person = new Person();
		person.setName("Joe Shmoe");
		person.setAddress("1234 AnyStreet");
		person.setPhone("555-1234");
		person.setFamily(List.of("Jane Schmoe", "John Schmoe"));
		return person;
	}
}

@Data @EqualsAndHashCode(callSuper=false) class Student extends Person{

	private String studentId;
	private String major;

	public static Student createStudent(){

		Student student = new Student();
		student.setStudentId("ABC1234");
		student.setMajor("Mechanical Engineering");
		return student;
	}
}