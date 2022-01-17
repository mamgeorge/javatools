package utils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import static utils.UtilityMain.EOL;
import static utils.UtilityMain.TAB;

/*
	add 4 "lombok" refs & plugin to build?
	add "lombok.config" with "lombok.addLombokGeneratedAnnotation=true"?
	add @Getter @Setter @EqualsAndHashCode @NoArgsConstructor to POJO
*/
@Getter @Setter @EqualsAndHashCode
public class DbProfile {
	//
	public enum DATABASES {sqlite, mysql, derby, oracle, cassandra}

	private static final Logger LOGGER = Logger.getLogger(DbProfile.class.getName());
	private String className;
	private String connectionUrl;
	private String sqlSelect;
	private String username;
	private String password;

	private DbProfile dbProfile;

	private DbProfile(String className, String connectionUrl, String sqlSelect) {
		this.className = className;
		this.connectionUrl = connectionUrl;
		this.sqlSelect = sqlSelect;
	}

	public static DbProfile init(DATABASES dbType) {
		//
		String className = "";
		String connectionUrl = "";
		String sqlSelect = "";
		switch ( dbType ) {
			case sqlite:
				className = "org.sqlite.JDBC";
				connectionUrl = "jdbc:sqlite:C:/workspace/dbase/sqlite/chinook.db";
				sqlSelect = "SELECT * FROM customers WHERE Country = 'USA' ORDER BY LastName ASC";
				break;
			case mysql:
				className = "com.mysql.cj.jdbc.Driver"; // "com.mysql.jdbc.Driver";
				connectionUrl = "jdbc:mysql://localhost:3306/mydb";
				sqlSelect = "SELECT * FROM history WHERE id > 0 ORDER BY dateEnd";
				break;
			case derby:
				className = "org.apache.derby.jdbc.ClientDriver";
				connectionUrl = "jdbc:derby://localhost:1527/mydb";
				sqlSelect = "";
				break;
			case oracle:
				className = "oracle.jdbc.driver.OracleDriver";
				connectionUrl = "jdbc:oracle:thin:@localhost:1521:mydb";
				sqlSelect = "";
				break;
			case cassandra:
				className = "";
				connectionUrl = "";
				sqlSelect = "";
				break;
		}
		DbProfile dbProfile = new DbProfile(className, connectionUrl, sqlSelect);
		return dbProfile;
	}

	public String readDbLines( ) {
		//
		StringBuilder stringBuilder = new StringBuilder();
		String DLM = ",\t";
		try {
			Class.forName(className);
			Connection connection;
			if ( username == null || username.equals("") ) {
				connection = DriverManager.getConnection(connectionUrl);
			} else {
				connection = DriverManager.getConnection(connectionUrl, username, password);
			}
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlSelect);
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			// get column titles
			stringBuilder.append(EOL);
			for ( int ictr = 1; ictr < intColumnCount + 1; ictr++ ) {
				stringBuilder.append(resultSetMetaData.getColumnName(ictr)).append(TAB);
			}
			stringBuilder.append(EOL);
			// get rows
			Object object;
			while ( resultSet.next() ) {
				//
				stringBuilder.append(TAB);
				for ( int ictr = 1; ictr < intColumnCount + 1; ictr++ ) {
					object = resultSet.getObject(ictr);
					if ( object instanceof Clob ) {
						object = object.getClass().getName();
					}
					if ( object == null ) {
						object = "NULL";
					}
					if ( ictr < intColumnCount ) {
						stringBuilder.append(object).append(DLM);
					} else {
						stringBuilder.append(object);
					}
				}
				stringBuilder.append(EOL);
			}
		}
		catch (ClassNotFoundException | SQLException ex) {
			LOGGER.info("ERROR: " + ex.getMessage());
		}
		return stringBuilder.toString();
	}

	public static Class testClass(String className) {
		//
		Class clazz = null;
		try { clazz = Class.forName(className); }
		catch (ClassNotFoundException ex) {
			LOGGER.info("ERROR: (" + className + ") " + ex.getMessage());
		}
		return clazz;
	}
}
