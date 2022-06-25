package utils;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import samples.DbProfile;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DBaseTest {
	//
	private static final String EOL = "\n";
	private static final String DLM = " | ";

	@Test void test_readDbLines_sqlite( ) {
		//
		String dbName = "", host = "", username = "", password = "";
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.sqlite, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split("\n").length > 1);
	}

	@Test void test_readDbLines_mySql( ) {
		//
		// see: "C:\Program Files\MySQL\MySQL Server 8.0\mysql_options.txt" ; ren2shen1
		String dbName = "mysql", host = "localhost";
		String username = System.getenv("MYSQL_USER");
		String password = System.getenv("MYSQL_PASS");
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.mysql, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split("\n").length > 1);
	}

	@Test void test_readDbLines_oracle( ) {
		//
		String dbName = "XE", host = "localhost";
		String username = System.getenv("ORACLE_USER") + " as sysdba";
		String password = System.getenv("ORACLE_PASS");
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.oracle, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split("\n").length > 1);
	}

	@Test void test_readDbLines_oracle2( ) { }

	@Test @Disabled void test_readDbLines_mssql( ) {
		//
		String dbName = "AdventureWorks2019", host = "2021-MARTIN\\SQLEXPRESS", username = "", password = "";
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.mssql, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split("\n").length > 1);
	}

	@Test void test_sqlite_full( ) {
		//
		String txtLines = "\n";
		String dbName = "chinook.db";
		String dbUrl = "jdbc:sqlite:C:/workspace/dbase/sqlite/" + dbName;
		String sqlDefault = "SELECT * FROM employees WHERE BirthDate > '1964-01-01' ORDER BY LastName ASC";
		try {
			Connection connection = DriverManager.getConnection(dbUrl);
			PreparedStatement preparedStatement = connection.prepareStatement(sqlDefault);
			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			while ( resultSet.next() ) {
				for ( int ictr = 1; ictr < intColumnCount + 1; ictr++ ) {
					txtLines += resultSet.getString(ictr) + DLM;
				}
				txtLines += EOL;
			}
		}
		catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split("\n").length > 1);
	}

	@Test void read_MongoDB_full( ) {
		//
		// https://docs.mongodb.com/drivers/java/sync/current/fundamentals/connection/connect/
		// mongodb://127.0.0.1:27017/?directConnection=true&serverSelectionTimeoutMS=2000&appName=mongosh+1.1.9
		String txtLines = "";
		String host = "localhost";
		String port = "27017";
		String database = "admin";
		String collection = "employees";
		//
		MongoClient mongoClient = new MongoClient(host, Integer.valueOf(port));
		MongoDatabase mongoDatabase = mongoClient.getDatabase(database);
		MongoCollection<Document> mongoCollection =
			mongoDatabase.getCollection(collection); // createCollection
		long lngCount = mongoCollection.countDocuments();
		MongoCursor<Document> mongoCursor = mongoCollection.find().iterator();
		//
		StringBuilder stringBuilder = new StringBuilder();
		Document document = null;
		String firstNames = "";
		while ( mongoCursor.hasNext() ) {
			document = mongoCursor.next(); // toJson()
			firstNames += document.getString("FIRST_NAME") + DLM;
			stringBuilder.append(document.get("LAST_NAME") + DLM);
			//stringBuilder.append(document.toString()+ EOL);
		}
		//
		txtLines += "lngCount     : " + lngCount + EOL;
		txtLines += "firstNames   : " + firstNames + EOL;
		txtLines += "sb.toString(): " + stringBuilder + EOL;
		txtLines += "document.toString(): " + document.toString() + EOL;
		txtLines += "document.toJson()  : " + document.toJson() + EOL;
		System.out.println(txtLines);
		assertTrue(txtLines.split("\n").length > 1);
	}
}
