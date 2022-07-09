package utils;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.hazelcast.map.IMap;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import samples.DbProfile;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static samples.DbProfile.ERROR_NOT_INTEGRATED;
import static samples.DbProfile.ERROR_NO_CREDENTIALS;
import static samples.DbProfile.ERROR_PKIX_CERT_PATH;
import static samples.DbProfile.ERROR_SSL_ENCRYPT;

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
		assertNotNull(txtLines);
	}

	@Test void test_readDbLines_mySql( ) {
		//
		// see: "C:\Program Files\MySQL\MySQL Server 8.0\mysql_options.txt" ; ren2shen1
		String dbName = "mydb", host = "localhost";
		String username = System.getenv("MYSQL_USER");
		String password = System.getenv("MYSQL_PASS");
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.mysql, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void test_readDbLines_oracle( ) {
		//
		String dbName = "XE", host = "localhost";
		String username = System.getenv("ORACLE_USER");
		String password = System.getenv("ORACLE_PASS");
		System.out.println("credentials: " + username + " / " + password);
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.oracle, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void test_readDbLines_oracleTns( ) {
		//
		String username = System.getenv("ORACLE_USER");
		String password = System.getenv("ORACLE_PASS");
		System.out.println("credentials: " + username + " / " + password);
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.oracleTns, "", "");
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void test_readDbLines_mssql( ) {
		//
		String dbName = "AdventureWorks2019", host = "2021-MARTIN\\SQLEXPRESS";
		String username = "", password = "";
		DbProfile.setupLibraryPath();
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.mssql, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split(EOL).length > 1);
	}

	//#### FULL SAMLPLES ####
	@Test void test_sqlite_full( ) {
		//
		String txtLines = EOL;
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
		assertTrue(txtLines.split(EOL).length > 1);
	}

	@Test void test_mssql_full( ) {
		//
		String dbName = "AdventureWorks2019", host = "2021-MARTIN\\SQLEXPRESS" + ";";
		String username = "", password = "";
		String dbUrl = "jdbc:sqlserver://" + host
			+ "databaseName=mydb;"
			+ "trustServerCertificate=true;"
			+ "integratedSecurity=true;";
		String sqlDefault = "SELECT TOP (10) * FROM [" + dbName + "].[Person].[Address];";
		System.out.println("dbUrl: " + dbUrl);
		String txtLines = "";
		DbProfile.setupLibraryPath();
		try {
			Connection connection = DriverManager.getConnection(dbUrl);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlDefault);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			while ( resultSet.next() ) {
				for ( int ictr = 1; ictr < intColumnCount + 1; ictr++ ) {
					txtLines += resultSet.getString(ictr) + DLM;
				}
				txtLines += EOL;
			}
		}
		catch (SQLException ex) {
			String err = ex.getMessage();
			if ( err.startsWith(ERROR_SSL_ENCRYPT) || err.contains(ERROR_PKIX_CERT_PATH) ) {
				System.out.println("ERROR missing trustServerCertificate: " + err);
			} else if ( err.startsWith(ERROR_NOT_INTEGRATED) ) {
				System.out.println("ERROR missing valid sqljdbc_auth.dll: " + err);
			} else if ( err.startsWith(ERROR_NO_CREDENTIALS) ) {
				System.out.println("ERROR missing credentials (user/pass): " + err);
			} else {
				System.out.println("ERROR: " + err);
			}
		}
		System.out.println(txtLines);
		assertNotNull(txtLines);
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
		assertTrue(txtLines.split(EOL).length > 1);
	}

	@Test void read_HikariCP( ) {
		//
		String txtLines = EOL;
		String dbURL = "jdbc:mysql://localhost:3306/mydb";
		String sqlDefault = "SELECT * FROM mydb.history WHERE id > 0 ORDER BY dateend;";
		String username = System.getenv("MYSQL_USER");
		String password = System.getenv("MYSQL_PASS");
		/*
			HikariConfig hikariConfig = new  HikariConfig( properties );
			HikariConfig hikariConfig = new  HikariConfig( "datasource.properties" );
			dataSourceClassName = com.mysql.cj.jdbc.Driver;
			dataSource.user = anyuser;
			datasource.cachePrepStmts = true;
		*/
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setJdbcUrl(dbURL);
		hikariConfig.setUsername(username);
		hikariConfig.setPassword(password);
		hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		//
		HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
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
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}

	@Test void read_Hazelcast( ) {
		//
		String txtLines = "";
		Config config = new Config();
		HazelcastInstance HCI = Hazelcast.newHazelcastInstance(config); // create new node in cluster
		Cluster cluster = HCI.getCluster(); // gets cluster in node
		Set<Member> setMembers = cluster.getMembers(); // get all devices
		ExecutorService executorService = HCI.getExecutorService("exec"); // get ExecutorService for cluster
		System.out.println("setMembers: " + setMembers);
		//
		for ( int ictr = 0; ictr < setMembers.size(); ictr++ ) {
			// send a task for each member on service of HazelcastInstance
			ClusterWorkingTask CWT = new ClusterWorkingTask();
			Future<String> future = executorService.submit(CWT);
			txtLines = "";
			try {
				txtLines += future.get() + EOL;
			}
			catch (InterruptedException | ExecutionException ex) {
				System.out.println("ERROR: " + ex.getMessage());
			}
		}
		// hazelcastInstance.shutDown();
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}

	@Test void read_HazelcastNW( ) {
		//
		String txtLines = "";
		String HOST_NAME = "localhost";
		String clusterName = "anyMap";
		int PORT = 5071;
		//
		// config
		Config config = new Config();
		NetworkConfig networkConfig = config.getNetworkConfig();
		networkConfig.setPort(PORT).setPortCount(5);
		networkConfig.setPortAutoIncrement(true);
		JoinConfig joinConfig = networkConfig.getJoin();
		joinConfig.getMulticastConfig().setEnabled(true);
		joinConfig.getTcpIpConfig().addMember(HOST_NAME).setEnabled(true);
		//
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
		IMap<Long, String> iMap = hazelcastInstance.getMap(clusterName);
		FlakeIdGenerator FIG = hazelcastInstance.getFlakeIdGenerator("newid");
		for ( int ictr = 0; ictr < 10; ictr++ ) {
			iMap.put(FIG.newId(), "message" + ictr);
		}
		// client
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.setClusterName("dev");
		HazelcastInstance hazelcastInstanceClient = HazelcastClient.newHazelcastClient(clientConfig);
		Map<Long, String> map = hazelcastInstanceClient.getMap("data");
		for ( Map.Entry<Long, String> entry : map.entrySet() ) {
			//
			txtLines += "\t" + entry.toString() + EOL;
		}
		//
		// hazelcastInstance.shutDown();
		System.out.println(txtLines);
		assertNotNull(txtLines);
	}
}

class ClusterWorkingTask implements Callable<String>, Serializable {
	@Override
	public String call( ) throws Exception { return "Hello World!"; }
}
