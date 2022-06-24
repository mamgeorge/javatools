package samples;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import oracle.jdbc.datasource.impl.OracleDataSource;
import utils.UtilityMain;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_KRB5_MUTUAL;
import static oracle.jdbc.OracleConnection.CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_SERVICES;
import static oracle.net.ano.AnoServices.AUTHENTICATION_KERBEROS5;

@Getter @Setter @EqualsAndHashCode @NoArgsConstructor
public class DbProfile {
	//
	public enum DBTYPE {sqlite, mysql, oracle, oracle2, mssql, mongodb}

	public static final String PATH_TNSURL = "C:/workspace/dbase/oracle/tnsnames.ora";
	public static final String PATH_KRB5_CONF = "C:/Users/mamge/Kerberos/config/krb5.conf";
	public static final String EOL = "\n";
	public static final String DLM = " | ";
	//
	private DbProfile dbProfile;
	private DBTYPE dbType;
	private String server;
	private String serverInstance;
	private String port;
	private String dbName;
	private String dbUrl;
	private String sqlDefault;

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
			String PATH_DEFAULT_SQLITE = "C:/workspace/dbase/sqlite/";
			if ( dbName == null || dbName.equals("") ) { dbName = "chinook.db"; }
			if ( host == null || host.equals("") ) { server = PATH_DEFAULT_SQLITE; }
			dbUrl = "jdbc:sqlite:" + server + dbName;
			sqlDefault = "SELECT * FROM customers WHERE Country = 'USA' ORDER BY LastName ASC";
		}
		if ( dbType.equals(DBTYPE.mysql) ) { }
		if ( dbType.equals(DBTYPE.oracle) ) { }
		if ( dbType.equals(DBTYPE.mssql) ) { }
		if ( dbType.equals(DBTYPE.mongodb) ) { }
	}

	public String readDbLines(String username, String password) {
		//
		String txtLines = "";
		Connection connection = null;
		Statement statement = null;
		try {
			if ( dbType == DBTYPE.oracle ) {
				OracleDataSource ODS = new OracleDataSource();
				ODS.setURL(dbUrl);
				ODS.setUser(username);
				ODS.setPassword(password);
			} else if ( dbType == DBTYPE.oracle2 ) {
				Properties propsKerberos = getOCI_KerberosProps(PATH_KRB5_CONF);
				try { Class.forName(oracle.jdbc.OracleDriver.class.getName()); }
				catch (ClassNotFoundException ex) { System.out.println("ERROR: " + ex.getMessage()); }
				connection = DriverManager.getConnection(dbUrl, propsKerberos);
			} else if ( username == null || username.equals("") ) {
				txtLines += "NO PASSWORD!";
				connection = DriverManager.getConnection(dbUrl);
			} else {
				connection = DriverManager.getConnection(dbUrl, username, password);
			}
			statement = connection.createStatement();
			txtLines += getResults(statement, sqlDefault);
		}
		catch (SQLException ex) { System.out.println("ERROR: " + ex.getMessage()); } finally {
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
		//
		Properties properties = new Properties();
		String CPTNAKM_AK = "( " + AUTHENTICATION_KERBEROS5 + " )";
		properties.setProperty(CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_SERVICES, CPTNAKM_AK);
		properties.setProperty(CONNECTION_PROPERTY_THIN_NET_AUTHENTICATION_KRB5_MUTUAL, "true");
		System.setProperty("java.security.krb5.conf", pathKrb5);
		return properties;
	}

	public static String getOCI_TnsUrl(String pathTns) {
		//
		String tnsFile = UtilityMain.getFileLocal(pathTns);
		String tnsUrlPrefix = "jdbc:oracle:thin:@";
		String tnsUrl = tnsUrlPrefix + tnsFile;
		return tnsUrl;
	}
}
