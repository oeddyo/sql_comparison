package edu.rutgers.cs541;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class DBStructure {

	// not sure if rsCol will be changed
	ResultSet rsCol;
	Vector<String> columnNames;
	Vector<Integer> dataTypes;
	Vector<Boolean> isNullables;

	DBStructure(String tableName, Statement stmt) {
		try {
			rsCol = getTableStruture(tableName, stmt);
			parseTableStruture(rsCol);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ResultSet getTableStruture(String tableName, Statement stmt)
			throws SQLException {
		ResultSet rsCol = stmt
				.executeQuery("SELECT column_name, data_type, is_nullable "
						+ "FROM information_schema.columns "
						+ "WHERE table_schema = 'PUBLIC' "
						+ "  AND table_name = '" + tableName + "'"
						+ "ORDER BY ordinal_position");
		return rsCol;
	}

	private void parseTableStruture(ResultSet rsCol) {
		// Note this function can be called only once for each table!
		// Because the pointer rsCol may change (I am not sure), I set it to
		// private
		columnNames = new Vector<String>();
		dataTypes = new Vector<Integer>();
		isNullables = new Vector<Boolean>();
		try {
			while (rsCol.next()) {
				columnNames.add(rsCol.getString(1));
				dataTypes.add(rsCol.getInt(2));
				isNullables.add(rsCol.getBoolean(3));
			}
			rsCol.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Vector<String> getColumnNames() {
		return columnNames;
	}

	public Vector<Integer> getDataTypes() {
		return dataTypes;
	}

	public Vector<Boolean> getIsNullables() {
		return isNullables;
	}
}
