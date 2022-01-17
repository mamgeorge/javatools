package utils;

import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static utils.DbProfile.DATABASES;

public class DbProfileTest {

	static final Logger LOGGER = Logger.getLogger(DbProfileTest.class.getName());
	static final String FRMT = "\t%-10s %s\n";
	static final String PATHFILE_LOCAL = "src/test/resources/";
	static final String ASSERT_MSG = "ASSERT_MSG";

	@Test void testClass( ) {
		//
		String txtLines = "";
		Class clazz;
		//
		clazz = DbProfile.testClass(DbProfile.init(DATABASES.sqlite).getClassName());
		txtLines += String.format(FRMT, "sqlite", clazz);
		//
		clazz = DbProfile.testClass(DbProfile.init(DATABASES.mysql).getClassName());
		txtLines += String.format(FRMT, "mysql", clazz);
		//
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_sqlite( ) {
		//
		DbProfile dbProfile = DbProfile.init(DATABASES.sqlite);
		dbProfile.setSqlSelect("SELECT FirstName || ' ' || LastName as NAME, Email FROM  customers " +
			"WHERE State = 'CA' ORDER BY LastName ASC");
		//
		String txtLines = dbProfile.readDbLines();
		//
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_mysql( ) {
		//
		DbProfile dbProfile = DbProfile.init(DATABASES.mysql);
		dbProfile.setUsername(System.getenv("MYSQL_USER"));
		dbProfile.setPassword(System.getenv("MYSQL_PASS"));
		//
		String txtLines = dbProfile.readDbLines();
		//
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}
}
