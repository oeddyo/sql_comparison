package edu.rutgers.cs541;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	private Worker mWorker;
	private Vector<String> mTableNames;
	private Vector<Vector<String>> mSolution;
	private boolean mIsMinimize;

	/**
	 * Loads the H2 Driver and initializes a database
	 * 
	 * Note - This should be called only once
	 * 
	 * @return a ReturnValue with a text reason on failure, an exception may
	 *         also be returned in the return value
	 */

	public Vector<String> getAllTableNames() {
		return mTableNames;
	}

	public Vector<String> getAllTuplesFromTable(String table) {
		for (int i = 0; i < mTableNames.size(); i++) {
			if (table.equals(mTableNames.get(i))) {
				return mSolution.get(i);
			}

		}
		return mSolution.lastElement();
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
	public SwingWorker<ReturnValue, Object> getCompareWorker(String schema,
			String query1, String query2, boolean isMinimize) {
		mWorker = new Worker(schema, query1, query2);
		mIsMinimize = isMinimize;
		return mWorker;
	}

	public SwingWorker<ReturnValue, Object> minimizeSolution() {
		return mWorker;

	}

	/**
	 * This class that will be returned by getCompareWorker()
	 */
	private class Worker extends SwingWorker<ReturnValue, Object> {
		private String mSchema;
		private String mQuery1;
		private String mQuery2;

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

		@Override
		protected ReturnValue doInBackground() throws Exception {

			// clear out anything still left in the DB
			// (note that we do this in a lazy manner)
			mStatement.execute("DROP ALL OBJECTS");

			// Unlike phase 1, the script is not given to us in a file,
			// so we would have to write it to file in order to
			// execute RUNSCRIPT
			// we can avoid the file using this function from the H2 api
			RunScript.execute(mConnection, new StringReader(mSchema));

			// see what tables are in the schema
			// (note that the user schema is called PUBLIC by default)
			ResultSet rsTab = mStatement.executeQuery("SELECT table_name "
					+ "FROM information_schema.tables "
					+ "WHERE table_schema = 'PUBLIC'");
			mTableNames = new Vector<String>();
			while (rsTab.next()) {
				// note that column indexing starts from 1
				mTableNames.add(rsTab.getString(1));
			}
			rsTab.close();

			DBOperation dbp = new DBOperation();

			Vector<Vector<Integer>> dataTypeVV = new Vector<Vector<Integer>>();
			Vector<Vector<Boolean>> isNullableVV = new Vector<Vector<Boolean>>();
			for (String tableName : mTableNames) {
				DBStructure dps = new DBStructure(tableName, mStatement);
				Vector<Integer> dataTypes = dps.getDataTypes();
				Vector<Boolean> isNullables = dps.getIsNullables();
				dataTypeVV.add(dataTypes);
				isNullableVV.add(isNullables);
			}
			Vector<Vector<String>> solution = new Vector<Vector<String>>();
			// tmp vector to store the tuples
			Strategy strt = new Strategy();

			// in this loop, we continually insert tuples into the tables until
			// either the user cancels,
			// or we find differences in the result sets of our two queries
			while (!isCancelled()) {

				while (!QueryComparison
						.bagCompare(mStatement, mQuery1, mQuery2)) {
					// try to insert tuple
					dbp.clearAllTables(mTableNames, mStatement);
					solution.clear();
					strt.changeIndex();
					for (int t = 0; t < mTableNames.size(); t++) {
						// read and parse the table
						String tableName = mTableNames.elementAt(t);
						DBStructure dps = new DBStructure(tableName, mStatement);
						Vector<Integer> dataTypes = dps.getDataTypes();
						Vector<Boolean> isNullables = dps.getIsNullables();
						Vector<String> insertedTuples = new Vector<String>();

						// insert 100 tuples for each table
						int dt = 0;
						while (true) {
							if (insertedTuples.size() == 10)
								break;
							dt++;
							if (dt == 50)
								break;
							StringBuilder tuple = dbp.generateTuple(dataTypes,
									isNullables, strt);
							StringBuilder insertSb = dbp
									.generateInsertStatement(tuple, tableName);

							try {
								mStatement.executeUpdate(insertSb.toString());
								insertedTuples.add(insertSb.toString());
							} catch (SQLException e) {
								System.out
										.println("Error: cannot insert tuples into db.");
							}
						}
						solution.add(insertedTuples);
					}
				}
				mSolution = solution;
				if (mIsMinimize) {
					mSolution = dbp.minimizeSolution(mSolution, mTableNames,
							mStatement, mQuery1, mQuery2);
				}
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				Script.execute(DB_URL, DB_USER, DB_PASSWORD, outputStream);
				return new ReturnValue(Code.SUCCESS, outputStream.toString());
			}

			// we are outside the loop, so the user must have canceled
			return new ReturnValue(Code.FAILURE, "No Results - Canceled");
		}
	}
}
