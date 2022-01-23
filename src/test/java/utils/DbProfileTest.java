package utils;

import org.junit.jupiter.api.Test;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static utils.UtilityMain.TAB;

public class DbProfileTest {

	private static final String EOL = "\n";
	private static final String DLM = " | ";

	@Test void test_DriverManager( ) {
		//
		StringBuilder stringBuilder = new StringBuilder();
		Enumeration<Driver> enumeration = DriverManager.getDrivers();
		Stream<Driver> stream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(enumeration.asIterator(), Spliterator.ORDERED),
			false
		);
		stream.forEach(str -> stringBuilder.append(str + EOL));
		//
		stringBuilder.append(EOL + DbProfile.testClassNames());
		//
		System.out.println(stringBuilder);
		assertNotNull(stringBuilder);
	}

	@Test void readDbLines_sqlite( ) {
		//
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.sqlite, "", "");
		String txtLines = dbProfile.readDbLines("", "");
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_mysql( ) {
		//
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.mysql, "localhost", "mydb");
		String txtLines = dbProfile.readDbLines(System.getenv("MYSQL_USER"), System.getenv("MYSQL_PASS"));
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_oracle( ) {
		//
		String username = System.getenv("ORACLE_USER") + " as sysdba";
		String password = System.getenv("ORACLE_PASS");
		//
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.oracle, "localhost", "XE");
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_mssql( ) {
		//
		String HOST = "2021-MARTIN\\SQLEXPRESS";
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.mssql, HOST, "mydb");
		String txtLines = dbProfile.readDbLines("", "");
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_mssqlFull( ) {
		//
		String txtLines = "";
		try {
			StringBuilder stringBuilder = new StringBuilder();
			String dbURL =
				"jdbc:sqlserver://2021-MARTIN\\SQLEXPRESS;databaseName=mydb;integratedSecurity=true";
			String sqlDefault = "SELECT TOP (10) * FROM Employee";
			//
			Connection connection = DriverManager.getConnection(dbURL);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlDefault);
			//
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			Object object;
			stringBuilder.append(EOL);
			while ( resultSet.next() ) {
				//
				stringBuilder.append(TAB);
				for ( int ictr = 1; ictr < intColumnCount + 1; ictr++ ) {
					object = resultSet.getObject(ictr);
					if ( object instanceof Clob ) { object = object.getClass().getName(); }
					if ( object == null ) { object = "NULL"; }
					if ( ictr < intColumnCount ) { stringBuilder.append(object).append(DLM); } else {
						stringBuilder.append(object);
					}
				}
				stringBuilder.append(EOL);
			}
			txtLines = stringBuilder.toString();
		}
		catch (SQLException ex) { System.out.println(ex.getMessage()); }
		System.out.println("txtLines: " + txtLines);
		// assertNotNull(txtLines);
	}
}
