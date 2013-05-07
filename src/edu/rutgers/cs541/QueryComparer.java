package edu.rutgers.cs541;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Vector;

import javax.swing.SwingWorker;

import org.h2.tools.RunScript;
import org.h2.tools.Script;

import edu.rutgers.cs541.ReturnValue.Code;

/**
 * This class encapsulates the Query Comparison logic Much of it is similar to
 * phase 1, except it continually adds rows to tables until the user cancels, or
 * an instance is found. It generates SwingWorker's that can be used to compare
 * the queries in order to avoid execution on the calling (i.e. GUI) thread.
 * 
 * @see getCompareWorker()
 */
public class QueryComparer {

	// This is the URL to create an H2 private In-Memory DB
	// unlike phase1, we actually name it ("db"), so that we
	// can connect to it later using Script.execute()
	private static final String DB_URL = "jdbc:h2:mem:db";

	// credentials do not really matter
	// since the database will be private
	private static final String DB_USER = "dummy";
	private static final String DB_PASSWORD = "password";

	// handles to our H2 database
	private Connection mConnection = null;
	private Statement mStatement = null;
	private Vector<String> mTableNames;
	private Vector<Vector<String>> mSolution;
	private String mSchema;
	private String mQuery1;
	private String mQuery2;
	private Vector<Vector<String>> mColumnNames;

	/**
	 * Loads the H2 Driver and initializes a database
	 * 
	 * Note - This should be called only once
	 * 
	 * @return a ReturnValue with a text reason on failure, an exception may
	 *         also be returned in the return value
	 */

	public boolean checkSchema(String schema) {
		if (schema.length() < 6) {
			return false;
		}
		try {
			mStatement.execute("DROP ALL OBJECTS");
			RunScript.execute(mConnection, new StringReader(schema));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public boolean checkQuery(String schema, String query) {
		if (query.length() < 6 || schema.length() < 6) {
			return false;
		}
		try {
			mStatement.execute("DROP ALL OBJECTS");
			RunScript.execute(mConnection, new StringReader(schema));
			Statement stmt = mConnection.createStatement();
			stmt.executeQuery(query);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public Vector<String> getAllTableNames() {
		return mTableNames;
	}

	public Vector<String> getAttributeNamesFromTable(String table) {
		for (int i = 0; i < mTableNames.size(); i++) {
			if (table.equals(mTableNames.get(i))) {
				return mColumnNames.get(i);
			}
		}
		return new Vector<String>();
	}

	public Vector<String> getAllTuplesFromTable(String table) {
		Vector<String> res = new Vector<String>();
		for (int i = 0; i < mTableNames.size(); i++) {
			if (table.equals(mTableNames.get(i))) {
				for (String tuple : mSolution.get(i)) {
					int left = tuple.indexOf('(');
					int right = tuple.indexOf(')');
					res.add(tuple.substring(left + 1, right));
				}
				break;
			}

		}
		return res;
	}

	public ReturnValue init() {

		// make sure it was not already called
		if (mConnection != null) {
			return new ReturnValue(Code.FAILURE, this.getClass().getName()
					+ ".init() already called");
		}

		// load the H2 Driver
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load H2 driver");
			return new ReturnValue(Code.FAILURE, this.getClass().getName()
					+ ".init() already called");
		}

		try {
			// create a connection to the H2 database
			// since the DB does not already exist, it will be created
			// automatically
			// http://www.h2database.com/html/features.html#in_memory_databases
			mConnection = DriverManager.getConnection(DB_URL, DB_USER,
					DB_PASSWORD);

			// create a statement to execute queries
			mStatement = mConnection.createStatement();
		} catch (SQLException e) {
			return new ReturnValue(Code.FAILURE,
					"Unable to initialize H2 database", e);
		}

		return new ReturnValue(Code.SUCCESS);
	}

	/**
	 * We do not want the GUI-thread to execute the search for an instance
	 * because then the GUI will not be usable by the user (s/he will not be
	 * able to cancel) So, we will instead use a Worker, which can be executed
	 * on a separate thread
	 * 
	 * see http://docs.oracle.com/javase/7/docs/api/javax/swing/SwingWorker.html
	 * http://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html
	 * 
	 * @param schema
	 *            - a list of SQL DDL statements to create the DB schema
	 * @param query1
	 *            - 1st of two SQL queries to test for differences
	 * @param query2
	 *            - 2nd of two SQL queries to test for differences
	 * @return a SwingWorker which can be executed by a worker thread
	 */
	public SwingWorker<ReturnValue, Object> getCreateSchemaWorker(String schema) {
		return new SchemaWorker(schema);
	}

	public SwingWorker<ReturnValue, Object> getCompareWorker(String schema,
			String query1, String query2) {
		return new Worker(schema, query1, query2);

	}

	public SwingWorker<ReturnValue, Object> getMinimizeWorker() {
		return new MinimizeWorker();
	}

	/**
	 * This class that will be returned by getCompareWorker()
	 */
	private class MinimizeWorker extends SwingWorker<ReturnValue, Object> {

		@Override
		protected ReturnValue doInBackground() throws Exception {
			// mStatement.execute("DROP ALL OBJECTS");
			// RunScript.execute(mConnection, new StringReader(mSchema));
			// for (int t = 0; t < mTableNames.size(); t++) {
			// for (int i = 0; i < mSolution.get(t).size(); i++) {
			// System.out.println(mSolution.get(t).get(i));
			// try {
			// mStatement.executeUpdate(mSolution.get(t).get(i));
			// } catch (SQLException e) {
			// System.out.println(e);
			// }
			// }
			// }
			DBOperation dbp = new DBOperation();
			mSolution = dbp.minimizeSolution(mSolution, mTableNames,
					mStatement, mQuery1, mQuery2);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Script.execute(DB_URL, DB_USER, DB_PASSWORD, outputStream);
			return new ReturnValue(Code.SUCCESS, outputStream.toString());
		}
	}

	private class SchemaWorker extends SwingWorker<ReturnValue, Object> {
		public SchemaWorker(String schema) {
			mSchema = schema;
			try {
				mStatement.execute("DROP ALL OBJECTS");
				RunScript.execute(mConnection, new StringReader(mSchema));
				ResultSet rsTab = mStatement.executeQuery("SELECT table_name "
						+ "FROM information_schema.tables "
						+ "WHERE table_schema = 'PUBLIC'");
				mTableNames = new Vector<String>();
				while (rsTab.next()) {
					// note that column indexing starts from 1
					mTableNames.add(rsTab.getString(1));
				}
				Collections.sort(mTableNames);
			} catch (Exception e) {
			}
			Collections.sort(mTableNames);
			mColumnNames = new Vector<Vector<String>>();
			for (String tableName : mTableNames) {
				DBStructure dps = new DBStructure(tableName, mStatement);
				Vector<String> columnNames = dps.getColumnNames();
				mColumnNames.add(columnNames);
			}
		}

		@Override
		protected ReturnValue doInBackground() throws Exception {
			// mStatement.execute("DROP ALL OBJECTS");
			// RunScript.execute(mConnection, new StringReader(mSchema));
			// ResultSet rsTab = mStatement.executeQuery("SELECT table_name "
			// + "FROM information_schema.tables "
			// + "WHERE table_schema = 'PUBLIC'");
			// mTableNames = new Vector<String>();
			// while (rsTab.next()) {
			// // note that column indexing starts from 1
			// mTableNames.add(rsTab.getString(1));
			// }
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			Script.execute(DB_URL, DB_USER, DB_PASSWORD, outputStream);
			return new ReturnValue(Code.SUCCESS, outputStream.toString());
		}
	}

	private class Worker extends SwingWorker<ReturnValue, Object> {

		/**
		 * Constructor for our Worker
		 * 
		 * @param schema
		 *            - a list of SQL DDL statements to create the DB schema
		 * @param query1
		 *            - 1st of two SQL queries to test for differences
		 * @param query2
		 *            - 2nd of two SQL queries to test for differences
		 */
		public Worker(String schema, String query1, String query2) {
			mSchema = schema;
			mQuery1 = query1;
			mQuery2 = query2;
		}

		/**
		 * The worker thread will execute this method This is where we do the
		 * computationally intensive search for an instance
		 * 
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		protected ReturnValue doInBackground() throws Exception {

			// clear out anything still left in the DB
			// (note that we do this in a lazy manner)

			Vector<GenerateAndTest> threads = new Vector<GenerateAndTest>();
			for (int i = 0; i < 2; i++) {
				String db_url = DB_URL + ((Integer) i).toString();
				GenerateAndTest my_thread = new GenerateAndTest(db_url,
						mSchema, mQuery1, mQuery2);
				threads.add(my_thread);
				my_thread.start();
			}

			while (!isCancelled()) {
				int finished_thread = -1;
				while (true) {
					for (int i = 0; i < threads.size(); i++) {
						if (threads.get(i).isFound()) {
							finished_thread = i;
							System.out.println("Thread "
									+ ((Integer) (finished_thread)).toString()
									+ " successfully gets the solution.");
							break;
						}
					}
					if (finished_thread != -1)
						break;
				}
				for (int i = 0; i < threads.size(); i++) {
					threads.get(i).Stop();
					System.out.println("Thread " + ((Integer) (i)).toString()
							+ " is killed.");
				}
				mSolution = threads.get(finished_thread).getSolution();

				mStatement.execute("DROP ALL OBJECTS");

				RunScript.execute(mConnection, new StringReader(mSchema));

				ResultSet rsTab = mStatement.executeQuery("SELECT table_name "
						+ "FROM information_schema.tables "
						+ "WHERE table_schema = 'PUBLIC'");
				mTableNames = new Vector<String>();
				while (rsTab.next()) {
					// note that column indexing starts from 1
					mTableNames.add(rsTab.getString(1));
				}
				rsTab.close();
				Collections.sort(mTableNames);
				mColumnNames = new Vector<Vector<String>>();
				for (String tableName : mTableNames) {
					DBStructure dps = new DBStructure(tableName, mStatement);
					Vector<String> columnNames = dps.getColumnNames();
					mColumnNames.add(columnNames);
				}

				for (Vector<String> sol : mSolution) {
					for (String insertSb : sol) {
						try {
							mStatement.executeUpdate(insertSb.toString());
						} catch (SQLException e) {
							System.out
									.println("We cannot never get here, if not bug.");
						}
					}
				}

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Script.execute(DB_URL, DB_USER, DB_PASSWORD, outputStream);
				return new ReturnValue(Code.SUCCESS, outputStream.toString());
			}
			for (int i = 0; i < threads.size(); i++) {
				threads.get(i).Stop();
			}
			return new ReturnValue(Code.FAILURE, "No Results - Canceled");
		}
	}
}
