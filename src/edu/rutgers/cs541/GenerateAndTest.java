package edu.rutgers.cs541;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.h2.tools.RunScript;

public class GenerateAndTest extends Thread {
	private String mDB_URL = "jdbc:h2:mem:db";
	private String mDB_USER = "dummy";
	private String mDB_PASSWORD = "password";
	private Connection mConnection = null;
	private Statement mStatement = null;
	private Vector<String> mTableNames;
	private Vector<Vector<String>> mSolution;
	private String mSchema;
	private String mQuery1;
	private String mQuery2;
	private boolean mFound;

	GenerateAndTest(String DB_URL) {
		mDB_URL = DB_URL;
		mFound = false;

		// load the H2 Driver
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			System.err.println("Unable to load H2 driver");
		}

		try {
			// create a connection to the H2 database
			// since the DB does not already exist, it will be created
			// automatically
			// http://www.h2database.com/html/features.html#in_memory_databases
			mConnection = DriverManager.getConnection(mDB_URL, mDB_USER,
					mDB_PASSWORD);

			// create a statement to execute queries
			mStatement = mConnection.createStatement();
		} catch (SQLException e) {
		}
	}

	// This is the entry point for the second thread.
	public void run() {
		try {

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
		} catch (SQLException e) {
		}

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

		while (!QueryComparison.bagCompare(mStatement, mQuery1, mQuery2)) {
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
					StringBuilder insertSb = dbp.generateInsertStatement(tuple,
							tableName);

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
		mFound = true;
		mSolution = solution;
	}

	public boolean isFound() {
		return mFound;
	}

	public Vector<Vector<String>> getSolution() {
		return mSolution;
	}
};
