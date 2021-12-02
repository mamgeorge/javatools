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
	public enum DATABASES {sqlite, derby, mysql, oracle, cassandra}

	private static final Logger LOGGER = Logger.getLogger(DbProfile.class.getName());
	private String className;
	private String connection;
	public String sql;

	private DbProfile dbProfile;

	private DbProfile(String className, String connection, String sql) {
		this.className = className;
		this.connection = connection;
		this.sql = sql;
	}

	public static DbProfile init(DATABASES dbtype) {
		//
		String className = "";
		String connection = "";
		String sql = "";
		switch (dbtype) {
			case sqlite:
				className = "org.sqlite.JDBC";
				connection = "jdbc:sqlite:C:/dbase/sqlite/chinook.db";
				sql = "SELECT * FROM customers WHERE Country = 'USA' ORDER BY LastName ASC";
				break;
			case derby:
				className = "org.apache.derby.jdbc.ClientDriver";
				connection = "jdbc:derby://localhost:1527/testDb";
				sql = "";
				break;
			case mysql:
				className = "com.mysql.jdbc.Driver";
				connection = "jdbc:mysql://ohit014:3306/db";
				sql = "";
				break;
			case oracle:
				className = "oracle.jdbc.driver.OracleDriver";
				connection = "jdbc:oracle:thin:@localhost:1521:db";
				sql = "";
				break;
			case cassandra:
				className = "";
				connection = "";
				sql = "";
				break;
		}
		DbProfile dbProfile = new DbProfile(className, connection, sql);
		return dbProfile;
	}

	public String dbRead() {
		//
		StringBuilder stringBuilder = new StringBuilder();
		String DLM = ",\t";
		try {
			Class.forName(getClassName());
			Connection connection = DriverManager.getConnection(getConnection());
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(getSql());
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
			int intColumnCount = resultSetMetaData.getColumnCount();
			// get column titles
			stringBuilder.append(EOL);
			for (int ictr = 1; ictr < intColumnCount + 1; ictr++) {
				stringBuilder.append(resultSetMetaData.getColumnName(ictr) + TAB);
			}
			stringBuilder.append(EOL);
			// get rows
			Object object = null;
			while (resultSet.next()) {
				//
				stringBuilder.append(TAB);
				for (int ictr = 1; ictr < intColumnCount + 1; ictr++) {
					object = resultSet.getObject(ictr);
					if (object instanceof Clob) {
						object = object.getClass().getName();
					}
					if (object == null) {
						object = "NULL";
					}
					if (ictr < intColumnCount) {
						stringBuilder.append(object + DLM);
					} else {
						stringBuilder.append(object);
					}
				}
				stringBuilder.append(EOL);
			}
		} catch (ClassNotFoundException | SQLException ex) {
			LOGGER.info(ex.getMessage());
		}
		return stringBuilder.toString();
	}
}
