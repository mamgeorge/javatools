package utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static utils.UtilityMain.TAB;

/*
	add 4 "lombok" refs & plugin to build?
	add "lombok.config" with "lombok.addLombokGeneratedAnnotation=true"?
	add @Getter @Setter @EqualsAndHashCode @NoArgsConstructor to POJO
	//
	many DB GUIs: IntelliJ_DBBrowser, DBeaver / SQuirrelSQL, DbVisualizer, Adminer, Firebird
	default GUIs: for Oracle: SqlDeveloper, MSSQL: SSMS (SQL_Server_Management_Studio)
*/
@Getter @Setter @EqualsAndHashCode @NoArgsConstructor
public class DbProfile {
	//
	private static final Logger LOGGER = Logger.getLogger(DbProfile.class.getName());

	public enum DBTYPE {sqlite, mysql, derby, h2, oracle, mssql, mongodb}

	public static final String EOL = "\n";
	public static final String DLM = " | ";

	private DbProfile dbProfile;
	private DBTYPE dbType;
	private String server;
	private String serverInstance;
	private String port;
	private String dbUrl;
	private String dbName;
	private String sqlDefault;

	public DbProfile(DBTYPE dbType, String host, String dbName) {
		//
		dbProfile = new DbProfile();
		this.dbType = dbType;
		this.server = host;
		this.serverInstance = host;
		this.dbName = dbName;
		//
		if ( host == null || host.equals("") ) { server = "localhost"; }
		if ( dbName == null || dbName.equals("") ) { dbName = "mydb"; }
		//
		switch ( dbType ) {
			case sqlite:
				// dbUrl = "jdbc:sqlite:C:/workspace/dbase/sqlite/chinook.db";
				String DEFAULT_PATH = "C:/workspace/dbase/sqlite/";
				if ( host == null || host.equals("") ) { server = DEFAULT_PATH; }
				if ( dbName == null || dbName.equals("") || dbName.equals("mydb") ) { dbName = "chinook.db"; }
				dbUrl = "jdbc:sqlite:" + server + dbName;
				sqlDefault = "SELECT * FROM employees WHERE BirthDate > '1964-01-01' ORDER BY LastName ASC";
				break;
			case mysql:
				// dbUrl = "jdbc:mysql://localhost:3306/mydb";
				port = "3306";
				dbUrl = "jdbc:mysql://" + server + ":" + port + "/" + dbName;
				sqlDefault = "SELECT * FROM history WHERE id > 0 ORDER BY dateEnd";
				break;
			case derby:
				// dbUrl = "jdbc:derby://localhost:1527/mydb";
				port = "1527";
				dbUrl = "jdbc:derby://" + server + ":" + port + "/" + dbName;
				sqlDefault = "";
				break;
			case h2:
				// jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1;INIT='classpath:/db.sql' // runscript
				break;
			case oracle:
				/*
					use ":" for SID, "/" for servicename
					dbUrl = "jdbc:oracle:thin@//localhost:1521/XE" ;
					String driver = "oracle.jdbc.driver.OracleDriver";
					OracleDataSource ODS = new OracleDataSource();
					default GUI: SqlDeveloper
				*/
				port = "1521";
				dbUrl = "jdbc:oracle:thin:@//" + server + ":" + port + "/" + dbName;
				sqlDefault = "SELECT * FROM (SELECT * FROM employees ORDER BY LAST_NAME) WHERE ROWNUM <= 10";
				break;
			case mssql:
				/*
					MSSQL required Microsoft JDBC Driver 6.0 for SQL Server Type 4
					implementation files('C:/workspace/dbase/mssql/sqljdbc6-4.2.jar')
					copies of sqljdbc_4.0/enu/auth/x64/sqljdbc_auth.dll to jdk bin, lib
					For DBBrowser dll needed to be in IntelliJ bin & lib
					Authentication	OS Credentials
					Driver_Source	External Library
					Driver_library	C:\workspace\dbase\mssql\sqljdbc6-4.2.jar
					Driver 			com.microsoft.sqlserver.jdbc.SQLServerDriver
					dbUrl = "jdbc:sqlserver://2021-MARTIN\SQLEXPRESS;databaseName=mydb;integratedSecurity=true"
					default GUI: SSMS (SQL_Server_Management_Studio)
				*/
				port = "1433";
				dbUrl = "jdbc:sqlserver://" + server + ";databaseName=" + dbName + ";integratedSecurity=true";
				sqlDefault = "SELECT TOP (10) * FROM [mydb].[dbo].[Employee]";
				break;
			case mongodb:
				// jdbc:cassandra:root/root@:/
				dbUrl = "";
				sqlDefault = "";
				break;
		}
		System.out.println("dbUrl: " + dbUrl);
		System.out.println("sqlDefault: " + sqlDefault);
	}

	public String readDbLines(String username, String password) {
		//
		StringBuilder stringBuilder = new StringBuilder();
		try {
			Connection connection;
			if ( username == null || username.equals("") ) {
				connection = DriverManager.getConnection(dbUrl);
			} else {
				connection = DriverManager.getConnection(dbUrl, username, password);
			}
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlDefault);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			// get column titles
			stringBuilder.append(EOL);
			for ( int ictr = 1; ictr < intColumnCount + 1; ictr++ ) {
				stringBuilder.append(resultSetMetaData.getColumnName(ictr)).append(TAB);
			}
			// get rows
			stringBuilder.append(EOL);
			Object object;
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
		}
		catch (SQLException ex) { LOGGER.info("ERROR: " + ex.getMessage()); }
		return stringBuilder.toString();
	}

	public static String testClassNames( ) {
		//
		// https://www.benchresources.net/jdbc-driver-list-and-url-for-all-databases/
		// DriverManager automatically selects driver className since JDBC4 (SDK6)
		// org.apache.derby.jdbc.ClientDriver()
		//
		String txtLines = "";
		try {
			DriverManager.registerDriver(new org.sqlite.JDBC());
			DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver()); // "com.mysql.jdbc.Driver";
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
			txtLines = "ALL DRIVERS RECOGNIZED!";
		}
		catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		return txtLines;
	}
}
