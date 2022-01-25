package utils;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
		stream.forEach(str -> stringBuilder.append(str).append(EOL));
		//
		stringBuilder.append(EOL).append(DbProfile.testClassNames());
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
		assertNotNull(txtLines);
	}

	// ############
	@Test void read_MongoDB( ) {
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
		assertNotNull(txtLines);
	}

	@Test void read_HikariCP( ) {
		//
		String txtLines = "\n";
		String dbUrl = "jdbc:mysql://localhost:3306/mydb";
		String sqlDefault = "SELECT * FROM history WHERE id > 0 ORDER BY dateEnd";
		String username = System.getenv("MYSQL_USER");
		String password = System.getenv("MYSQL_PASS");
		// username, password, jdbcUrl, dataSourceClassName
		// HikariConfig config = new HikariConfig( properties  );
		// HikariConfig config = new HikariConfig( "datasource.properties" );
		// dataSourceClassName = com.mysql.cj.jdbc.Driver
		// dataSource.user = anyuser
		// dataSource.cachePrepStmts = true
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(dbUrl);
		hikariConfig.setUsername(username);
		hikariConfig.setPassword(password);
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		//
		HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
		//
		try {
			Connection connection = hikariDataSource.getConnection();
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
		assertNotNull(txtLines);
	}
}
