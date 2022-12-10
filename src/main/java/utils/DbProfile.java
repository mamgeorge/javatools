package utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_SERVICES;
import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_KRB5_CC_NAME;
import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_KRB5_MUTUAL;
import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_THIN_NET_ENCRYPTION_LEVEL;
import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_THIN_NET_CHECKSUM_TYPES;
import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_THIN_NET_ALLOW_WEAK_CRYPTO;

import static oracle.net.ano.AnoServices.AUTHENTICATION_KERBEROS5;
import static oracle.net.ano.AnoServices.CHECKSUM_SHA1;
import static oracle.net.ano.AnoServices.ENCRYPTION_AES256;
import static oracle.net.ano.AnoServices.ENCRYPTION_AES128;

@Getter @Setter @EqualsAndHashCode @NoArgsConstructor
public class DbProfile {
	/*
		Normally DriverManager creates the connection and can be fed properties.
		When DataSource (or DriverManagerDataSource, JdbcTemplate) are used,
		the connection may be made based on the settings in app.props.
	*/
	public enum DBTYPE {sqlite, mysql, oracle, oracleTns, mssql, mongodb}

	public enum DBASES {

		// access, dbname, host, port, serviceName, sqlDefault, user, pass
		SQLITE_CHINOOK("NONE", "chinook", "C:/workspace/dbase/sqlite/", "", "",
			"SELECT * FROM customers WHERE Country = 'USA' ORDER BY LastName ASC", "", ""),
		MYSQL_MYDB("NONE", "mydb", "localhost", "3306", "",
			"SELECT * FROM mydb.history WHERE id > 0 ORDER BY dateend;", "", ""),
		ORACLE_XE("NONE", "XE", "localhost", "1521", "", "SELECT * FROM sys.employees ORDER BY LAST_NAME", "",
			"");

		public final String access;
		public final String dbname;
		public final String host;
		public final String port;
		public final String serviceName;
		public final String sqlDefault;
		public final String user;
		public final String pass;

		DBASES(String access, String dbname, String host, String port, String serviceName, String sqlDefault,
			String user, String pass) {
			this.access = access;
			this.dbname = dbname;
			this.host = host;
			this.port = port;
			this.serviceName = serviceName;
			this.sqlDefault = sqlDefault;
			this.user = user;
			this.pass = pass;
		}
	}

	static final String PATHFILE_LOCAL = "src/test/resources/"; // "C:/Users/mamge/Kerberos/config/"
	public static final String PATH_TNS_DEFAULT = PATHFILE_LOCAL + "local_tnsnames.ora";
	public static final String PATH_KRB5_CONF = PATHFILE_LOCAL + "krb5.conf";
	public static final String PATH_KRB5_CACHE = "C:/Users/mamgeorge/krb5cc_mamgeorge";

	public static final String EOL = "\n";
	public static final String DLM = " | ";

	public static final String ERROR_SSL_ENCRYPT =
		"The driver could not establish a secure connection to SQL Server by using Secure Sockets Layer (SSL) encryption.";
	public static final String ERROR_PKIX_CERT_PATH =
		"PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target";
	public static final String ERROR_NOT_INTEGRATED =
		"This driver is not configured for integrated authentication.";
	public static final String ERROR_NO_CREDENTIALS = "Login failed for user";
	//
	private DbProfile dbProfile;
	private DBTYPE dbType;
	private String server;
	private String serverInstance;
	private String port;
	private String dbName;
	private String dbUrl;
	private String sqlDefault;
	private Properties properties = null;

	public DbProfile(DBTYPE dbType, String host, String dbName) {
		//
		dbProfile = new DbProfile();
		//
		this.dbType = dbType;
		this.dbName = dbName;
		this.server = host;
		this.serverInstance = host;
		//
		if ( dbType.equals(DBTYPE.sqlite) ) {
			//
			if ( dbName == null || dbName.equals("") ) { dbName = DBASES.SQLITE_CHINOOK.dbname + ".db"; }
			if ( host == null || host.equals("") ) { server = DBASES.SQLITE_CHINOOK.host; }
			dbUrl = "jdbc:sqlite:" + server + dbName;
			sqlDefault = DBASES.SQLITE_CHINOOK.sqlDefault;
			//
			// not needed
			try { Class.forName(java.sql.DriverManager.class.getName()); }
			catch (ClassNotFoundException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		}
		if ( dbType.equals(DBTYPE.mysql) ) {
			port = "3306";
			dbUrl = "jdbc:mysql://" + server + ":" + port + "/" + dbName;
			sqlDefault = DBASES.MYSQL_MYDB.sqlDefault;
			//
			try { Class.forName(com.mysql.cj.jdbc.Driver.class.getName()); }
			catch (ClassNotFoundException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		}
		if ( dbType.equals(DBTYPE.oracle) ) {
			port = DBASES.ORACLE_XE.port;
			sqlDefault = "SELECT * FROM (" + DBASES.ORACLE_XE.sqlDefault + ") WHERE ROWNUM <= 10";
			dbUrl = "jdbc:oracle:thin:@" + server + ":" + port + ":" + dbName;
			//
			try { Class.forName(oracle.jdbc.OracleDriver.class.getName()); }
			catch (ClassNotFoundException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		}
		if ( dbType.equals(DBTYPE.oracleTns) ) {
			//
			sqlDefault = "SELECT * FROM (" + DBASES.ORACLE_XE.sqlDefault + ") WHERE ROWNUM <= 10";
			dbUrl = getOCI_TnsUrl(PATH_TNS_DEFAULT);
			properties = getOCI_KerberosProps(PATH_KRB5_CONF);
			properties = null;
			//
			try { DriverManager.registerDriver(new oracle.jdbc.OracleDriver()); }
			catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		}
		if ( dbType.equals(DBTYPE.mssql) ) {
			// jdbc:sqlserver://2021-MARTIN\SQLEXPRESS;databaseName=mydb;integratedSecurity=true
			// ensure "sqljdbc_auth.dll" is on library path for Java app
			port = "1433";
			dbUrl =
				"jdbc:sqlserver://" + host
					//	+ ":" + port
					+ ";"
					//	+ "database=AdventureWorks2019;" ?
					+ "databaseName=mydb;"
					//	+ "user=" + username + ";"; // yourusername@yourserver;"
					//	+ "password=" + password + ";"
					//	+ "encrypt=true;"
					+ "trustServerCertificate=true;"
					+ "integratedSecurity=true;"
			//	+ "loginTimeout=30;"
			;
			sqlDefault = "SELECT TOP (10) * FROM [" + dbName + "].[Person].[Address];";
			//
			System.out.println("dbUrl: " + dbUrl);
			properties = new Properties();
			properties.setProperty("integratedSecurity", "true");
			try { DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver()); }
			catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		}
		if ( dbType.equals(DBTYPE.mongodb) ) {
			//
		}
	}

	public String readDbLines(String username, String password) {
		//
		String txtLines = "";
		Connection connection = null;
		Statement statement = null;
		try {
			if ( properties != null ) {
				System.out.println("USING PROPERTIES!");
				connection = DriverManager.getConnection(dbUrl, properties);
			} else if ( username == null || username.equals("") ) {
				System.out.println("NO PASSWORD!");
				connection = DriverManager.getConnection(dbUrl);
			} else {
				System.out.println("USING PASSWORD!");
				connection = DriverManager.getConnection(dbUrl, username, password);
			}
			statement = connection.createStatement();
			txtLines += getResults(statement, sqlDefault);
		}
		catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		finally {
			if ( statement != null ) try { statement.close(); }
			catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
			if ( connection != null ) try { connection.close(); }
			catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		}
		return txtLines;
	}

	public static String getResults(Statement statement, String sql) {
		//
		StringBuilder stringBuilder = new StringBuilder();
		//
		try {
			ResultSet resultSet = statement.executeQuery(sql);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			//
			// get column names
			stringBuilder.append(EOL);
			for ( int nctr = 1; nctr < intColumnCount + 1; nctr++ ) {
				stringBuilder.append(resultSetMetaData.getColumnName(nctr)).append(DLM);
			}
			// get rows
			Object object;
			stringBuilder.append(EOL);
			while ( resultSet.next() ) {
				stringBuilder.append(DLM);
				for ( int cctr = 1; cctr < intColumnCount + 1; cctr++ ) {
					object = resultSet.getObject(cctr);
					if ( object instanceof Clob ) { object = object.getClass().getName(); }
					if ( object == null ) { object = "NULL"; }
					if ( cctr < intColumnCount ) { stringBuilder.append(object).append(DLM); } else {
						stringBuilder.append(object);
					}
				}
				stringBuilder.append(EOL);
			}
		}
		catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); }
		//
		return stringBuilder.toString();
	}

	public static Properties getOCI_KerberosProps(String pathKrb5) {

		// https://docs.oracle.com/en/database/oracle/oracle-database/21/jajdb/oracle/jdbc/OracleConnection.html
		Properties properties = new Properties();
		// properties.setProperty(CONNECTION_PROPERTY_THIN_NET_ENCRYPTION_LEVEL, "REQUIRED");
		// properties.setProperty(CONNECTION_PROPERTY_THIN_NET_ENCRYPTION_LEVEL, "( " + ENCRYPTION_AES256 + "," + ENCRYPTION_AES128 + " )");
		// properties.setProperty(CONNECTION_PROPERTY_THIN_NET_CHECKSUM_TYPES, "( " + CHECKSUM_SHA1 + " )");

		String KERBEROS5 = "(" + AUTHENTICATION_KERBEROS5 + ")";
		// oracle.net.authentication_services			KERBEROS5
		// oracle.net.kerberos5_mutual_authentication	true
		// oracle.net.allow_weak_crypto             	true
		// oracle.net.kerberos5_cc_name					pathkrb5cc
		properties.setProperty(CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_SERVICES, KERBEROS5);
		properties.setProperty(CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_KRB5_MUTUAL, "true");
		properties.setProperty(CONNECTION_PROPERTY_THIN_NET_ALLOW_WEAK_CRYPTO, "true");
		properties.setProperty(CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_KRB5_CC_NAME, PATH_KRB5_CACHE);

		// java.security.krb5.conf						pathKrb5
		System.setProperty("java.security.krb5.conf", pathKrb5);
		System.out.println("properties: " + properties);
		return properties;
	}

	public static String getOCI_TnsUrl(String pathTns) {
		//
		String WHITE_SPACE = "\\s";
		String tnsFile = UtilityMain.getFileLocal(pathTns);
		String tnsUrlPrefix = "jdbc:oracle:thin:@";
		String tnsUrl = ( tnsUrlPrefix + tnsFile ).replaceAll(WHITE_SPACE, "");
		System.out.println("tnsUrl: " + tnsUrl);
		return tnsUrl;
	}

	public static void setupLibraryPath( ) {
		/*
			note: MSSQL needs the "sqljdbc_auth.dll" library when reading Windows Authentication

			-Djava.library.path=
			C:/workspace/dbase/mssql/type4_sqljdbc642/sqljdbc_6.0/enu/auth/x64;
			C:/workspace/dbase/mssql/sqljdbc_10.2.1.0_enu/sqljdbc_10.2/enu/auth/x64;
			https://stackoverflow.com/questions/5419039/is-djava-library-path-equivalent-to-system-setpropertyjava-library-path
		*/
		try {
			String JAVA_LIB_PATH_MSSQL =
				"C:/workspace/dbase/mssql/sqljdbc_10.2.1.0_enu/sqljdbc_10.2/enu/auth/x64";
			System.setProperty("java.library.path", JAVA_LIB_PATH_MSSQL);
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		}
		catch (NoSuchFieldException | IllegalAccessException ex) {
			System.out.println("ERROR: " + ex.getMessage());
		}
	}
}
