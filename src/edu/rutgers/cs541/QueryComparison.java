package edu.rutgers.cs541;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is a sample class intended to demonstrate how to parse command-line
 * options and how to start an H2 In-Memory Database
 * 
 * You are free to change the contents of this file completely (and add any
 * additional classes), but your main() method must be in
 * edu.rutgers.cs541.EntryPoint
 * 
 * @author yaros
 */
public class QueryComparison {

	public static boolean bagCompare(Statement stmt, String query1,
			String query2) {
		boolean isDiff = false;
		try {

			String groupedQuery1 = getGroupedQuery(stmt, query1);
			String groupedQuery2 = getGroupedQuery(stmt, query2);

			// check if there are rows returned in query1 that are
			// not returned in query2
			ResultSet rsChk = stmt.executeQuery("SELECT ("
					+ "SELECT COUNT(*) AS diff12 FROM (" + groupedQuery1
					+ " EXCEPT " + groupedQuery2 + "))" + " + "
					+ "(SELECT COUNT(*) AS diff21 FROM (" + groupedQuery2
					+ " EXCEPT " + groupedQuery1 + "))");
			if (rsChk.next()) {
				int diffRows = rsChk.getInt(1);
				isDiff = diffRows > 0;
			}

		} catch (SQLException e) {
			System.err.println("Unable to perform check for query differences");
			e.printStackTrace();
		}
		return isDiff;
	}

	/**
	 * Augment the query by grouping all columns in the Result Set and adding a
	 * COUNT(*) field
	 * 
	 * @param stmt
	 *            - an active Statement to use for executing queries
	 * @param query
	 *            - the SQL query to be augmented
	 */
	private static String getGroupedQuery(Statement stmt, String query) {
		String rv = null;
		try {
			// execute the query and get the ResultSet's meta-data
			ResultSet rsQ = stmt.executeQuery(query);
			ResultSetMetaData rsMetaData = rsQ.getMetaData();

			// get a list of column labels so that we can group on all columns
			// backticks are used in case of labels that might be illegal when
			// reused in a query (e.g. COUNT(*))
			int columnCount = rsMetaData.getColumnCount();
			StringBuilder columnSeq = new StringBuilder();
			columnSeq.append('`').append(rsMetaData.getColumnLabel(1))
					.append('`');
			for (int i = 2; i <= columnCount; i++) {
				columnSeq.append(',');
				columnSeq.append('`').append(rsMetaData.getColumnLabel(i))
						.append('`');
			}
			rsQ.close();

			rv = "SELECT " + columnSeq + ", COUNT(*) " + "FROM (" + query
					+ ") " + "GROUP BY " + columnSeq;
		} catch (SQLException e) {
			System.err.println("Unable to perform check for query differences");
			e.printStackTrace();
		}
		return rv;
	}
}
