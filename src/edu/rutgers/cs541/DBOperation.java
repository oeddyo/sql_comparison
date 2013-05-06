package edu.rutgers.cs541;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Random;
import java.util.Vector;

public class DBOperation {

	Random randomGenerator;

	public DBOperation() {
		randomGenerator = new Random();
	}

	public StringBuilder generateTuple(Vector<Integer> dataTypes,
			Vector<Boolean> isNullables, Strategy strt) {
		// c is the number of columns

		int c = dataTypes.size();
		int dataType;
		StringBuilder tuple = new StringBuilder();
		boolean isNullable;
		// this method return a tuple as "xia", 123, 0.11, no parenthesis
		for (int i = 0; i < c; i++) {
			dataType = dataTypes.get(i);
			isNullable = isNullables.get(i);
			if (i > 0) {
				tuple.append(", ");
			}
			// generate a value appropriate for the column's type

			if (isNullable) {

				double setNull = randomGenerator.nextDouble();
				if (setNull < strt.getNullProbability()) {
					tuple.append("NULL");
					continue;
				}
			}
			if (dataType == Types.INTEGER) {
				int v = randomGenerator.nextInt(strt.getMaxInt());
				tuple.append(Integer.toString(v));
			} else if (dataType == Types.DOUBLE) {
				// double v = (randomGenerator.nextDouble() - 0.5) * intRange;
				int v = randomGenerator.nextInt(strt.getMaxInt());
				tuple.append(Integer.toString(v));
			} else if (dataType == Types.VARCHAR) {
				int len = randomGenerator.nextInt(strt.getMaxLength()) + 1;
				String v = "'"
						+ generateString(len, strt.getCandidateCharacter())
						+ "'";
				tuple.append(v);
			} else {
				// this should report error
			}
		}
		return tuple;
	}

	private String generateString(int length, String candidateCharacters) {
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = candidateCharacters.charAt(randomGenerator
					.nextInt(candidateCharacters.length()));
		}
		return new String(text);
	}

	public StringBuilder generateInsertStatement(StringBuilder tuple,
			String tableName) {
		StringBuilder insertSb = new StringBuilder();
		insertSb.append("INSERT INTO ");
		insertSb.append(tableName);
		insertSb.append(" VALUES (");
		insertSb.append(tuple);
		insertSb.append(")");
		return insertSb;
	}

	private StringBuilder generateDeleteStatement(String table) {
		StringBuilder delete = new StringBuilder();
		delete.append("DELETE FROM ");
		delete.append(table);
		return delete;
	}

	private void deleteAllTuplesFromSingleTable(String table, Statement stmt) {
		StringBuilder comm = generateDeleteStatement(table);
		try {
			stmt.executeUpdate(comm.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clearAllTables(Vector<String> tables, Statement stmt) {
		for (String table : tables) {
			deleteAllTuplesFromSingleTable(table, stmt);
		}
	}

	public Vector<Vector<String>> minimizeSolution(
			Vector<Vector<String>> solution, Vector<String> tables,
			Statement stmt, String query1, String query2) {
		// we assume in the H2 db, we have the separating tuples
		Vector<Vector<String>> res = new Vector<Vector<String>>();

		for (int it = 0; it < 16; it++) {
			for (int i = 0; i < solution.size(); i++) {
				String table = tables.elementAt(i);
				res.add(minimizeSingleTable(solution.elementAt(i), table, stmt,
						query1, query2));
			}
		}
		return res;
	}

	public Vector<String> minimizeSingleTable(Vector<String> tuples,
			String table, Statement stmt, String query1, String query2) {
		Vector<Vector<Integer>> q = new Vector<Vector<Integer>>();
		// whether to judge empty set works
		q.add(new Vector<Integer>());
		for (int i = 0; i < tuples.size(); i++) {
			Vector<Integer> v = new Vector<Integer>();
			v.add(i);
			q.add(v);
		}
		int h = 0;
		while (h < q.size()) {
			Vector<Integer> last = q.elementAt(h);
			Vector<String> subTuples = new Vector<String>();
			for (Integer i : last) {
				subTuples.add(tuples.elementAt(i));
			}
			if (check(subTuples, table, stmt, query1, query2)) {
				return subTuples;
			}
			int start = -1;
			if (h > 0) {
				start = last.elementAt(last.size() - 1);
			}
			for (int i = start + 1; i < tuples.size(); i++) {
				Vector<Integer> v = new Vector<Integer>();
				for (Integer j : q.elementAt(h)) {
					v.add(j);
				}
				v.add(i);
				q.add(v);
			}
			h++;
			// Debug.out(h);
		}
		// Vector<String> subTuples = new Vector<String>();
		// for (String tuple : tuples)
		// subTuples.add(tuple);
		return tuples;
	}

	public void printNumberOfTuples(String table, Statement stmt) {
		ResultSet rs;
		try {
			rs = stmt.executeQuery("select count(*) from " + table);
			if (rs.next())
				Debug.out(rs.getString(1));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveResultToFile(String path, Statement stmt) {
		try {
			// Use another handy H2 command to save the instance
			stmt.execute("SCRIPT NOSETTINGS TO '" + path + "'");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean check(Vector<String> tuples, String table, Statement stmt,
			String query1, String query2) {
		deleteAllTuplesFromSingleTable(table, stmt);
		for (String insertSb : tuples) {
			try {
				stmt.executeUpdate(insertSb);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return QueryComparison.bagCompare(stmt, query1, query2);
	}

	public static void main(String[] args) {
	}
}
