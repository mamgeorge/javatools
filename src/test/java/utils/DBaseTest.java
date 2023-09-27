package utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import samples.DBClusterWorkingTask;
import samples.DBRowMapperChinook;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static utils.DbProfile.DLM;
import static utils.DbProfile.ERROR_NOT_INTEGRATED;
import static utils.DbProfile.ERROR_NO_CREDENTIALS;
import static utils.DbProfile.ERROR_PKIX_CERT_PATH;
import static utils.DbProfile.ERROR_SSL_ENCRYPT;
import static utils.DbProfile.getDataSource_DM;
import static utils.DbProfile.getDataSource_EMB;
import static utils.DbProfile.showGenericQuery;
import static utils.UtilityMain.EOL;
import static utils.UtilityMain.exposeObject;

class DBaseTest {

	@BeforeAll static void initJdbcLogging( ) {

		// initJdbcLogging
		Logger LOGGER = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
		LOGGER.setLevel(Level.toLevel("error"));
	}

	// simplified
	@Test void dataSource_DM( ) {
		//
		String dbName = DbProfile.DBASES.SQLITE_CHINOOK.dbname + ".db";
		String dbUrl = "jdbc:sqlite:" + DbProfile.DBASES.SQLITE_CHINOOK.host + dbName;

		DriverManagerDataSource dataSource = getDataSource_DM(dbUrl);
		System.out.println(exposeObject(dataSource));
		assertNotNull(dataSource);
	}

	@Test void dataSource_EMB_H2( ) {

		EmbeddedDatabaseType EDT = EmbeddedDatabaseType.H2;
		DataSource dataSource = getDataSource_EMB(EDT);
		String sql = "SELECT * FROM simple";

		StringBuilder stringBuilder = showGenericQuery(dataSource, sql, DLM);
		System.out.println(stringBuilder);
		assertNotNull(dataSource);
	}

	@Test void jdbcTemplate_queryWithMapper( ) {

		String dbName = DbProfile.DBASES.SQLITE_CHINOOK.dbname + ".db";
		String dbUrl = "jdbc:sqlite:" + DbProfile.DBASES.SQLITE_CHINOOK.host + dbName;
		String sqlPrepare = "SELECT * FROM customers WHERE Country = ? ORDER BY LastName ASC";
		String parameter = "USA";

		DriverManagerDataSource dataSource = getDataSource_DM(dbUrl);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		DBRowMapperChinook dbRowMapperChinook = new DBRowMapperChinook();
		List<String> list = jdbcTemplate.query(sqlPrepare, dbRowMapperChinook, parameter);

		StringBuilder stringBuilder = new StringBuilder(EOL + DLM);
		list.forEach(rows -> stringBuilder.append(rows).append(DLM));
		System.out.println(stringBuilder);
		assertNotNull(stringBuilder);
	}

	@Test void jdbcTemplate_queryForList( ) {

		String dbName = DbProfile.DBASES.SQLITE_CHINOOK.dbname + ".db";
		String dbUrl = "jdbc:sqlite:" + DbProfile.DBASES.SQLITE_CHINOOK.host + dbName;
		String sql = "SELECT * FROM customers WHERE Country = 'USA' ORDER BY LastName ASC";

		DriverManagerDataSource dataSource = getDataSource_DM(dbUrl);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);

		StringBuilder stringBuilder = new StringBuilder(EOL);
		list.stream().forEach(rows -> {
			rows.values().stream().forEach(col -> stringBuilder.append(col + DLM));
			stringBuilder.append(EOL);
		});
		System.out.println(stringBuilder);
		assertNotNull(stringBuilder);
	}

	@Test @Disabled( "Requires a DB with Procedure!" ) void dataSource_simpleJdbcCall( ) {
		//
		String txtLines = "";
		String dbName = DbProfile.DBASES.SQLITE_CHINOOK.dbname + ".db";
		String dbUrl = "jdbc:sqlite:" + DbProfile.DBASES.SQLITE_CHINOOK.host + dbName;
		String sqlDefault = DbProfile.DBASES.SQLITE_CHINOOK.sqlDefault;
		String sqlPrepare = "SELECT * FROM customers WHERE Country = ? ORDER BY LastName ASC";
		String parameter = "USA";
		//
		DriverManagerDataSource dataSource = getDataSource_DM(dbUrl);
		//
		// https://www.codejava.net/frameworks/spring/spring-simplejdbccall-examples
		// REQUIRES A PROCEDURE
		SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(dataSource);
		simpleJdbcCall.withProcedureName("sample");
		MapSqlParameterSource MSPS = new MapSqlParameterSource();
		MSPS.addValue("Country", parameter);
		Map<String, Object> map = simpleJdbcCall.execute(MSPS);
		Collection<Object> collection = map.values();
		for ( Object object : collection ) { txtLines += object.toString() + EOL; }

		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	// dbProfile & readDbLines
	@Test void readDbLines_sqlite( ) {
		//
		String dbName = "", host = "", username = "", password = "";
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.sqlite, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_mySql( ) {
		//
		// see: "C:\Program Files\MySQL\MySQL Server 8.0\mysql_options.txt" ; ren2shen1
		String dbName = DbProfile.DBASES.MYSQL_MYDB.dbname;
		String host = DbProfile.DBASES.MYSQL_MYDB.host;
		String username = System.getenv("MYSQL_USER");
		String password = System.getenv("MYSQL_PASS");
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.mysql, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_oracle( ) {
		//
		String dbName = "XE";
		String host = DbProfile.DBASES.ORACLE_XE.host;
		String username = System.getenv("ORACLE_USER");
		String password = System.getenv("ORACLE_PASS");
		System.out.println("credentials: " + username + " / " + password);
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.oracle, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test void readDbLines_oracleTns( ) {
		//
		String username = System.getenv("ORACLE_USER");
		String password = System.getenv("ORACLE_PASS");
		System.out.println("credentials: " + username + " / " + password);
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.oracleTns, "", "");
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test @Disabled( "" ) void readDbLines_mssql( ) {
		//
		String dbName = "AdventureWorks2019", host = "2021-MARTIN\\SQLEXPRESS";
		String username = "", password = "";
		DbProfile.setupLibraryPath();
		DbProfile dbProfile = new DbProfile(DbProfile.DBTYPE.mssql, host, dbName);
		String txtLines = dbProfile.readDbLines(username, password);
		System.out.println("txtLines: " + txtLines);
		assertTrue(txtLines.split(EOL).length > 1);
	}

	// connections & clients
	@Test void connection_sqlite( ) {
		//
		StringBuilder stringBuilder = new StringBuilder(EOL);
		String dbName = DbProfile.DBASES.SQLITE_CHINOOK.dbname + ".db";
		String dbUrl = "jdbc:sqlite:" + DbProfile.DBASES.SQLITE_CHINOOK.host + dbName;
		String sqlDefault = DbProfile.DBASES.SQLITE_CHINOOK.sqlDefault;
		try {
			Connection connection = DriverManager.getConnection(dbUrl);
			PreparedStatement preparedStatement = connection.prepareStatement(sqlDefault);
			ResultSet resultSet = preparedStatement.executeQuery();
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			while ( resultSet.next() ) {
				for ( int ictr = 1; ictr < intColumnCount + 1; ictr++ ) {
					stringBuilder.append(resultSet.getString(ictr)).append(DLM);
				}
				stringBuilder.append(EOL);
			}
		}
		catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		System.out.println("stringBuilder: " + stringBuilder);
		assertTrue(stringBuilder.length() > 1);
	}

	@Test void connection_oracle( ) {
		//
		String txtLines = EOL;
		String dbName = DbProfile.DBASES.ORACLE_XE.dbname;
		String host = DbProfile.DBASES.ORACLE_XE.host;
		String port = DbProfile.DBASES.ORACLE_XE.port;
		String sqlDefault =
			"SELECT * FROM (" + DbProfile.DBASES.ORACLE_XE.sqlDefault + ") WHERE ROWNUM <= 10";
		String dbUrl = "jdbc:oracle:thin:@" + host + ":" + port + ":" + dbName;
		String username = System.getenv("ORACLE_USER");
		String password = System.getenv("ORACLE_PASS");
		System.out.println("credentials: " + username + " / " + password);
		//
		try {
			Class.forName(oracle.jdbc.OracleDriver.class.getName());
			Connection connection = DriverManager.getConnection(dbUrl, username, password);
			Statement statement = connection.createStatement();
			txtLines += DbProfile.getResults(statement, sqlDefault);
		}
		catch (ClassNotFoundException | SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		//
		System.out.println("txtLines: " + txtLines);
		assertNotNull(txtLines);
	}

	@Test @Disabled( "avail!" ) void connection_mySQL( ) {

		// connection to active AWS RDS MySQL DB
		String host = "database-1.clzyctwoju9i.us-east-1.rds.amazonaws.com";
		String dbName = "RDS_MYSQL";
		String username = "admin";
		String password = "adminrds";
		String dbUrl = "jdbc:mysql://" + host
			+ "/" + dbName + "?user=" + username + "&password=" + password;
		String sqlDefault = "SELECT * FROM simple;";
		System.out.println("dbUrl: " + dbUrl);

		String txtLines = "";
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

	@Test void client_mongoDB( ) {
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

	@Test void client_hikariDB( ) {
		// CP: Connection Pooling
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

	@Test @Disabled( "time!" ) void connection_msSQL( ) {

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

	@Test @Disabled( "time!" ) void hazelcast( ) {
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
			DBClusterWorkingTask DBCWT = new DBClusterWorkingTask();
			Future<String> future = executorService.submit(DBCWT);
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

	@Test @Disabled( "time!" ) void hazelcastNW( ) {
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
