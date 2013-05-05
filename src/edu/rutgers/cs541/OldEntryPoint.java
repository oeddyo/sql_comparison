package edu.rutgers.cs541;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
public class OldEntryPoint {

	/**
	 * This is the main method, where execution begins
	 * 
	 * @param args
	 *            - command line arguments
	 */

	public static void main(String[] args) {

		if (args.length != 4) {
			String usage = "required arguments are <schema_file> "
					+ "<query1> <query2> <instance_output_directory>";
			System.err.println(usage);
			// croak
			System.exit(1);
		}

		// read the queries from file
		String query1 = readFileOrDie(args[1]);
		String query2 = readFileOrDie(args[2]);

		// load the H2 Driver
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Error: Unable to load H2 driver");
			e.printStackTrace();
			// croak
			System.exit(1);
		}

		// This is the URL to create an H2 private In-Memory DB
		String dbUrl = "jdbc:h2:mem:";

		// credentials do not really matter
		// since the database will be private
		String dbUser = "dummy";
		String dbPassword = "password";

		Connection conn = null;
		Statement stmt = null;

		try {
			// create a connection to the H2 database
			// since the DB does not already exist, it will be created
			// automatically
			// http://www.h2database.com/html/features.html#in_memory_databases
			conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

			// create a statement to execute queries
			stmt = conn.createStatement();
		} catch (SQLException e) {
			System.out.println("Error: Unable to initialize H2 database");
			e.printStackTrace();
		}

		// H2 has a nice command allowing us to run a series of
		// queries from file.
		// http://www.h2database.com/html/grammar.html#runscript
		// We will use it here to run the user-supplied schema script.
		try {
			stmt.execute("RUNSCRIPT FROM '" + args[0] + "'");
		} catch (SQLException e) {
			System.out.println("Error: Unable to load the schema script \""
					+ args[0] + "\"");
			e.printStackTrace();
		}

		// We will now query the system views (i.e. the information_schema)
		// to see what is in the user provided schema.
		try {
			// see what tables are in the schema
			// (note that the user schema is called PUBLIC by default)
			ResultSet rsTab = stmt.executeQuery("SELECT table_name "
					+ "FROM information_schema.tables "
					+ "WHERE table_schema = 'PUBLIC'");

			Vector<String> tableNames = new Vector<String>();
			while (rsTab.next()) {
				// note that column indexing starts from 1
				tableNames.add(rsTab.getString(1));
			}
			rsTab.close();

			DBOperation dbp = new DBOperation();

			Vector<Vector<Integer>> dataTypeVV = new Vector<Vector<Integer>>();
			Vector<Vector<Boolean>> isNullableVV = new Vector<Vector<Boolean>>();
			for (String tableName : tableNames) {
				DBStructure dps = new DBStructure(tableName, stmt);
				Vector<Integer> dataTypes = dps.getDataTypes();
				Vector<Boolean> isNullables = dps.getIsNullables();
				dataTypeVV.add(dataTypes);
				isNullableVV.add(isNullables);
			}
			Vector<Vector<String>> solution = new Vector<Vector<String>>();
			// tmp vector to store the tuples
			Strategy strt = new Strategy();
			while (!QueryComparison.bagCompare(stmt, query1, query2)) {
				dbp.clearAllTables(tableNames, stmt);
				solution.clear();
				strt.changeIndex();
				for (int t = 0; t < tableNames.size(); t++) {
					// read and parse the table
					String tableName = tableNames.elementAt(t);
					DBStructure dps = new DBStructure(tableName, stmt);
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
						StringBuilder insertSb = dbp.generateInsertStatement(
								tuple, tableName);

						try {
							stmt.executeUpdate(insertSb.toString());
							insertedTuples.add(insertSb.toString());
						} catch (SQLException e) {
							System.out
									.println("Error: cannot insert tuples into db.");
						}
					}
					solution.add(insertedTuples);
				}
			}
			String outPath = new File(args[3], "first_slution.sql").getPath();
			dbp.saveResultToFile(outPath, stmt);
			// dbp.clearAllTables(tableNames, stmt);
			// minimize the number of effective tuples
			solution = dbp.minimizeSolution(solution, tableNames, stmt, query1,
					query2);
			for (Vector<String> subTuples : solution) {
				Debug.out(subTuples.size());
			}
			// for (Vector<String> inserts : solution) {
			// for (String insert : inserts) {
			// stmt.executeUpdate(insert);
			// }
			// }
			// use the user-supplied directory (last command line argument)
			// for (String table : tableNames) {
			// Debug.out(table);
			// dbp.printNumberOfTuples(table, stmt);
			// }
			outPath = new File(args[3], "optimal_solution.sql").getPath();
			dbp.saveResultToFile(outPath, stmt);
		} catch (SQLException e) {
			System.out.println("Error: Unable to generate instance");
			e.printStackTrace();
		}

	}

	/**
	 * Read the contents of a file into a string Terminate program if unable to
	 * do so
	 * 
	 * @param fileName
	 *            - name of file to be read
	 */
	private static String readFileOrDie(String fileName) {
		// using fast way to read a file into a string:
		// http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file/326440#326440

		String rv = null;

		FileInputStream stream = null;
		try {
			stream = new FileInputStream(new File(fileName));
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			rv = Charset.defaultCharset().decode(bb).toString();
		} catch (IOException e) {
			System.out.println("Error: Unable to open file \"" + fileName
					+ "\"");
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		if (rv == null) {
			// must not have been able to read file, so croak
			System.exit(1);
		}

		return rv;
	}
}
