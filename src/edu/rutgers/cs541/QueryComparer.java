package edu.rutgers.cs541;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.SwingWorker;

import org.h2.tools.RunScript;
import org.h2.tools.Script;

import edu.rutgers.cs541.ReturnValue.Code;


/**
 * This class encapsulates the Query Comparison logic
 * Much of it is similar to phase 1, except it continually adds
 *  rows to tables until the user cancels, or an instance is found.
 * It generates SwingWorker's that can be used to compare the queries
 *  in order to avoid execution on the calling (i.e. GUI) thread.
 * @see getCompareWorker()
 */
public class QueryComparer {

	// This is the URL to create an H2 private In-Memory DB
	//  unlike phase1, we actually name it ("db"), so that we
	//  can connect to it later using Script.execute()
	private static final String DB_URL = "jdbc:h2:mem:db";

	// credentials do not really matter 
	// since the database will be private
	private static final String DB_USER = "dummy";
	private static final String DB_PASSWORD = "password";

	//handles to our H2 database
	private Connection mConnection = null;
	private Statement mStatement = null;
	
	// A random generator we will use to create tuples
	private Random mRandom = new Random(0);

	
	/**
	 * Loads the H2 Driver and initializes a database
	 * 
	 * Note - This should be called only once
	 * 
	 * @return a ReturnValue with a text reason on failure, 
	 *              an exception may also be returned in the return value
	 */
	public ReturnValue init() {
		
		//make sure it was not already called
		if(mConnection != null) {
			return new ReturnValue(Code.FAILURE, 
					this.getClass().getName()+".init() already called");
		}

		// load the H2 Driver
		try {
			Class.forName("org.h2.Driver");
		} catch(ClassNotFoundException e) {
			System.err.println("Unable to load H2 driver");
			return new ReturnValue(Code.FAILURE, 
					this.getClass().getName()+".init() already called");
		}


		try {
			//create a connection to the H2 database
			//since the DB does not already exist, it will be created automatically
			//http://www.h2database.com/html/features.html#in_memory_databases
			mConnection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

			//create a statement to execute queries
			mStatement = mConnection.createStatement();
		} catch (SQLException e) {
			return new ReturnValue(Code.FAILURE,
					"Unable to initialize H2 database",
					e);
		}

		return new ReturnValue(Code.SUCCESS);
	}
	
	
	/**
	 * We do not want the GUI-thread to execute the search for an instance
	 *  because then the GUI will not be usable by the user 
	 *  (s/he will not be able to cancel)
	 * So, we will instead use a Worker, which can be executed on a separate 
	 *  thread
	 * 
	 * see 
	 * http://docs.oracle.com/javase/7/docs/api/javax/swing/SwingWorker.html
	 * http://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html
	 * 
	 * @param schema - a list of SQL DDL statements to create the DB schema
	 * @param query1 - 1st of two SQL queries to test for differences
	 * @param query2 - 2nd of two SQL queries to test for differences
	 * @return a SwingWorker which can be executed by a worker thread
	 */
	public SwingWorker<ReturnValue, Object> getCompareWorker(
			String schema, String query1, String query2) {
		return new Worker(schema, query1, query2);
	}

	
	/**
	 * This class that will be returned by getCompareWorker()
	 */
	private class Worker extends SwingWorker<ReturnValue, Object>{
		private String mSchema;
		private String mQuery1;
		private String mQuery2;

		/**
		 * Constructor for our Worker
		 * 
		 * @param schema - a list of SQL DDL statements to create the DB schema
		 * @param query1 - 1st of two SQL queries to test for differences
		 * @param query2 - 2nd of two SQL queries to test for differences
		 */
		public Worker(String schema, String query1, String query2) {
			mSchema = schema;
			mQuery1 = query1;
			mQuery2 = query2;
		}

		/**
		 * The worker thread will execute this method
		 * This is where we do the computationally intensive search for 
		 *  an instance
		 * @see javax.swing.SwingWorker#doInBackground()
		 */
		@Override
		protected ReturnValue doInBackground() throws Exception {
			
			//clear out anything still left in the DB
			//  (note that we do this in a lazy manner)
			mStatement.execute("DROP ALL OBJECTS");

			// Unlike phase 1, the script is not given to us in a file,
			//  so we would have to write it to file in order to 
			//   execute RUNSCRIPT
			//  we can avoid the file using this function from the H2 api
			RunScript.execute(mConnection, new StringReader(mSchema));

			// see what tables are in the schema 
			//  (note that the user schema is called PUBLIC by default) 
			ResultSet rsTab = mStatement.executeQuery(
					"SELECT table_name "+
							"FROM information_schema.tables "+
					"WHERE table_schema = 'PUBLIC'");
			List<String> tableNames = new ArrayList<String>();
			while(rsTab.next()) {
				//note that column indexing starts from 1
				tableNames.add(rsTab.getString(1));
			}
			rsTab.close();

			// in this loop, we continually insert tuples into the tables until
			//  either the user cancels, 
			//  or we find differences in the result sets of our two queries
			while(!isCancelled()) {

				for(String tableName : tableNames) {
					//for each table in the schema, 
					//  we will generate an INSERT statement to create a tuple
					StringBuilder insertSb = new StringBuilder();
					insertSb.append("INSERT INTO ");
					insertSb.append(tableName);
					insertSb.append(" VALUES (");

					// query for the columns of the current table
					ResultSet rsCol = mStatement.executeQuery(
							"SELECT column_name, data_type, is_nullable "+
									"FROM information_schema.columns "+
									"WHERE table_schema = 'PUBLIC' "+
									"  AND table_name = '"+tableName+"'" +
							"ORDER BY ordinal_position");
					int colNum = 1;
					while(rsCol.next()) {
						String columnName = rsCol.getString(1);
						int dataType = rsCol.getInt(2);
						boolean isNullable = rsCol.getBoolean(3);

						if(colNum++ != 1) {
							insertSb.append(", ");
						}

						if(isNullable && mRandom.nextBoolean()) {
							insertSb.append("NULL");
							continue;
						}

						// generate a value appropriate for the column's type
						switch(dataType) {
						case Types.INTEGER:
							insertSb.append(mRandom.nextInt()); 
							break;
						case Types.DOUBLE:
							insertSb.append(mRandom.nextDouble()); 
							break;
						case Types.VARCHAR:
							insertSb.append("'");
							char randChar = (char)(97 + mRandom.nextInt(26));
							insertSb.append(randChar);
							insertSb.append("'");
							break;
						default:
							return new ReturnValue(Code.FAILURE,
									"Column \""+columnName
									+"\" of Table "+tableName
									+" has unsupported data type "+dataType);
						}
					}
					insertSb.append(")");
					rsCol.close();

					//execute the INSERT statement to add a tuple to the table
					mStatement.executeUpdate(insertSb.toString());

					String groupedQuery1 = getGroupedQuery(mQuery1);
					String groupedQuery2 = getGroupedQuery(mQuery2);

					// check if there are rows returned in query1 that are 
					//   not returned in query2 
					ResultSet rsChk = mStatement.executeQuery(
						"SELECT ("+
								"SELECT COUNT(*) AS diff12 FROM ("+
								groupedQuery1+" EXCEPT "+groupedQuery2+"))" +
								" + "+
								"(SELECT COUNT(*) AS diff21 FROM ("+
								groupedQuery2+" EXCEPT "+groupedQuery1+"))");
					rsChk.next();
					int diffRows = rsChk.getInt(1);
					rsChk.close();
					if(diffRows > 0) {
						//we have found an instance such 
						// that the two queries are different
						// let's return them to the user
						
						//like with RUNSCRIPT above, we want to avoid the use
						// of a file, so we can use Script.execute()
						ByteArrayOutputStream outputStream 
							= new ByteArrayOutputStream();
						Script.execute(DB_URL, DB_USER, DB_PASSWORD, outputStream);
						return new ReturnValue(Code.SUCCESS, outputStream.toString());
					}
				}
			}
			//we are outside the loop, so the user must have canceled
			return new ReturnValue(Code.FAILURE, "No Results - Canceled");
		}

		/**
		 * Augment the query by grouping all columns in the Result Set 
		 *  and adding a COUNT(*) field
		 * @param stmt - an active Statement to use for executing queries
		 * @param query - the SQL query to be augmented 
		 */
		private String getGroupedQuery(String query) {
			String rv = null;
			try {
				//execute the query and get the ResultSet's meta-data
				ResultSet rsQ = mStatement.executeQuery(query);
				ResultSetMetaData rsMetaData = rsQ.getMetaData();

				//get a list of column labels so that we can group on all columns
				// backticks are used in case of labels that might be illegal when
				//  reused in a query (e.g. COUNT(*))
				int columnCount = rsMetaData.getColumnCount();
				StringBuilder columnSeq = new StringBuilder();
				columnSeq.append('`').append(rsMetaData.getColumnLabel(1)).append('`');
				for(int i=2; i<=columnCount; i++) {
					columnSeq.append(',');
					columnSeq.append('`').append(rsMetaData.getColumnLabel(i)).append('`');
				}
				rsQ.close();

				rv =  "SELECT "+columnSeq+", COUNT(*) "
						+ "FROM ("+query+") "
						+ "GROUP BY "+columnSeq;
			} catch (SQLException e) {
				System.err.println("Unable to perform check for query differences");
				e.printStackTrace();
			}
			return rv;
		}
	}
}
