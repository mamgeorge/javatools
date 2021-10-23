package utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import samples.BooksCatalog;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.UtilityMain.EOL;

// C:/workspace/training/javatools/src/test/java/utils
public class JsonTasksTest {
	//
	private static final String BASE_PATH = "src/test/resources/";
	private static final String jsonFile = BASE_PATH + "booksCatalog.json";
	private String json = "";

	@BeforeEach void init() {
		//
		json = UtilityMain.getFileLocal(jsonFile, "");
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
}
