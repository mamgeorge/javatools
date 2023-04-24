package utils;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static utils.DbProfile.DLM;
import static utils.DbProfile.EOL;

public class DBRowMapperChinook implements RowMapper<String> {

	// public RowMapperChinook( ) { super(); }

	@Override public String mapRow(ResultSet resultSet, int rowNum) throws SQLException {

		String txtLine = "";
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		int rsColumnCount = resultSetMetaData.getColumnCount();

		for ( int colCtr = 1; colCtr < rsColumnCount; ++colCtr ) {
			txtLine += String.format("%-4s" + DLM, resultSet.getString(colCtr) );
		}
		return txtLine + EOL;
	}
}
