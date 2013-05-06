package edu.rutgers.cs541;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import edu.rutgers.cs541.ReturnValue.Code;

/**
 * This class is the main form that will be visible to the user
 * 
 */
public class MainWindow {

	// this is a handle to the H2-back query comparison engine
	// (that you want to improve)
	private QueryComparer mQueryComparer;

	// a handle for any worker that is executing
	private SwingWorker<ReturnValue, Object> mCurrentWorker;

	// this instance of a listener will be called when the
	// worker finished execution
	private QueryCompareListener mCompareListener = new QueryCompareListener();

	// get the OS-dependent line separator
	private String lineSeparator = System.getProperty("line.separator");

	private JFrame mMonteCarloQueryForm;
	private JLabel mSchemaLabel;
	private JTextArea mSchemaTextArea;
	private JLabel mQuery1Label;
	private JTextPane mQuery1TextArea;
	private JLabel mQuery2Label;
	private JTextPane mQuery2TextArea;
	private JLabel mOutputLabel;
	private JTextArea mOutputTextArea;
	private JButton mStartButton;
	private JButton mCancelButton;
	private JButton mStartMinimalButton;
	private JButton mCreateSchema;
	private Vector<String> tablesList;

	private JList mTableList;

	private JPanel tableListPanel;

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * This code was generated by the Designer in Eclipse, then slightly
	 * modified.
	 */
	private void initialize() {
		mMonteCarloQueryForm = new JFrame();
		mMonteCarloQueryForm.setResizable(true);
		mMonteCarloQueryForm.setTitle("Monte Carlo Query Comparer");
		mMonteCarloQueryForm.setBounds(100, 100, 777, 714);
		mMonteCarloQueryForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mMonteCarloQueryForm.getContentPane().setLayout(null);

		mSchemaLabel = new JLabel("Schema ");
		mSchemaLabel.setBounds(22, 13, 222, 16);
		mSchemaLabel.setHorizontalAlignment(SwingConstants.LEFT);
		mMonteCarloQueryForm.getContentPane().add(mSchemaLabel);

		mSchemaTextArea = new JTextArea();
		mSchemaLabel.setLabelFor(mSchemaTextArea);
		mSchemaTextArea.setBounds(22, 32, 720, 152);
		JScrollPane schemaTextAreaScrollPane = new JScrollPane(mSchemaTextArea);
		schemaTextAreaScrollPane.setBounds(22, 32, 720, 152);
		mMonteCarloQueryForm.getContentPane().add(schemaTextAreaScrollPane);

		mQuery1Label = new JLabel("Query1");
		mQuery1Label.setBounds(22, 197, 131, 16);
		mMonteCarloQueryForm.getContentPane().add(mQuery1Label);

		mQuery1TextArea = new JTextPane();
		mQuery1Label.setLabelFor(mQuery1TextArea);
		mQuery1TextArea.getDocument().addDocumentListener(
				new SyntaxHighlighter(mQuery1TextArea));

		mQuery1TextArea.setBounds(22, 215, 350, 225);

		JScrollPane query1TextAreaScrollPane = new JScrollPane(mQuery1TextArea);
		query1TextAreaScrollPane.setBounds(22, 215, 350, 225);
		mMonteCarloQueryForm.getContentPane().add(query1TextAreaScrollPane);

		mQuery2Label = new JLabel("Query2");
		mQuery2Label.setBounds(392, 197, 131, 16);
		mMonteCarloQueryForm.getContentPane().add(mQuery2Label);

		mQuery2TextArea = new JTextPane();
		mQuery2Label.setLabelFor(mQuery2TextArea);
		mQuery2TextArea.setBounds(22, 215, 350, 225);
		mQuery2TextArea.getDocument().addDocumentListener(
				new SyntaxHighlighter(mQuery2TextArea));

		JScrollPane query2TextAreaScrollPane = new JScrollPane(mQuery2TextArea);

		query2TextAreaScrollPane.setBounds(392, 215, 350, 225);
		mMonteCarloQueryForm.getContentPane().add(query2TextAreaScrollPane);

		mStartButton = new JButton("Start Analysis");
		mStartButton.addActionListener(new StartButtonActionListener());
		mStartButton.setBounds(150, 453, 122, 25);
		mMonteCarloQueryForm.getContentPane().add(mStartButton);

		mStartMinimalButton = new JButton("Start Minimal Analysis");
		mStartMinimalButton.addActionListener(new StartMinimalActionListener());
		mStartMinimalButton.setBounds(300, 453, 122, 25);
		mMonteCarloQueryForm.getContentPane().add(mStartMinimalButton);

		mCancelButton = new JButton("Cancel");
		mCancelButton.setBounds(450, 453, 97, 25);
		mCancelButton.addActionListener(new CancelButtonActionListener());
		mCancelButton.setEnabled(false);
		mMonteCarloQueryForm.getContentPane().add(mCancelButton);

		mCreateSchema = new JButton("create schema");
		mCreateSchema.setBounds(550, 453, 97, 25);
		mCreateSchema.addActionListener(new CreateSchema());
		mCreateSchema.setEnabled(true);
		mMonteCarloQueryForm.getContentPane().add(mCreateSchema);

		mOutputTextArea = new JTextArea();
		mOutputTextArea.setEditable(false);
		mOutputTextArea.setBounds(22, 506, 500, 150);
		JScrollPane outputTextAreaScrollPane = new JScrollPane(mOutputTextArea);
		outputTextAreaScrollPane.setBounds(22, 506, 500, 150);
		mMonteCarloQueryForm.getContentPane().add(outputTextAreaScrollPane);

		mOutputLabel = new JLabel("Output");
		mOutputLabel.setLabelFor(mOutputTextArea);
		mOutputLabel.setBounds(22, 486, 104, 16);
		mMonteCarloQueryForm.getContentPane().add(mOutputLabel);

		JPanel tableListPanel = new JPanel();
		tableListPanel.setLayout(new BorderLayout());
		// Create a new listbox control
		String listData[] = { "aa", "bb", "cccsfddsf" };
		Vector<String> tmpDataVector = new Vector<String>();
		tmpDataVector.addAll(Arrays.asList(listData));
		setPopulatedJList(tmpDataVector);

		// refer http://www.cs.cf.ac.uk/Dave/HCI/HCI_Handout_CALLER/node143.html

		// initialize the QueryComparer (& H2 DB)
		mQueryComparer = new QueryComparer();
		ReturnValue rv = mQueryComparer.init();
		if (rv.getCode() != Code.SUCCESS) {
			// this should not happen, but would likely be due
			// to an H2 initialization problem
			mStartButton.setEnabled(false);
			putReturnValueContentsInOutputWindow(rv);
		}
	}

	private void setPopulatedJList(Vector<String> listData) {
		// mMonteCarloQueryForm.getContentPane().remove(mTableList);
		System.out.println(listData.get(0));
		mTableList = new JList(listData);
		// JList listbox = new JList( listData );
		JPanel tableListPanel = new JPanel();
		tableListPanel.add(mTableList, BorderLayout.CENTER);
		JScrollPane tableListlScrollPane = new JScrollPane(tableListPanel);
		tableListlScrollPane.setBounds(560, 522, 200, 80);
		// mTableList.setBounds(560,522,200,80);
		tableListlScrollPane.setBounds(560, 522, 200, 80);

		mMonteCarloQueryForm.getContentPane().add(tableListlScrollPane);
	}

	/**
	 * This ActionListener will be called when the user clicks the start button
	 * 
	 */

	private class CreateSchema implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// prevent the user from clicking start again
			System.out.println("in function");
			mStartMinimalButton.setEnabled(false);
			// clear any old output
			mOutputTextArea.setText("");

			// get the schema and queries from their respective textboxes
			String schema = mSchemaTextArea.getText();
			mCurrentWorker = mQueryComparer.getCreateSchemaWorker(schema);
			// create a worker to test these user inputs
			// mCurrentWorker = mQueryComparer.getCompareWorker(schema, query1,
			// query2, true);

			// set the callback (PropertyChangeListener) for the worker
			mCurrentWorker.addPropertyChangeListener(mCompareListener);

			// start the worker (executes on a worker thread)
			mCurrentWorker.execute();

			// allow the user to click the cancel button
			mCancelButton.setEnabled(true);
			try {
				Thread.sleep(0);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			tablesList = mQueryComparer.getAllTableNames();
			for (String tn : tablesList)
				System.out.println("table: " + tn);
			System.out.println(tablesList.get(0));

			setPopulatedJList(tablesList);

		}
	}

	private class StartMinimalActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// prevent the user from clicking start again
			mStartMinimalButton.setEnabled(false);
			// clear any old output
			mOutputTextArea.setText("");

			// create a worker to test these user inputs
			mCurrentWorker = mQueryComparer.getMinimizeWorker();

			// set the callback (PropertyChangeListener) for the worker
			mCurrentWorker.addPropertyChangeListener(mCompareListener);

			// start the worker (executes on a worker thread)
			mCurrentWorker.execute();

			// allow the user to click the cancel button
			mCancelButton.setEnabled(true);
		}
	}

	private class StartButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// prevent the user from clicking start again
			mStartButton.setEnabled(false);

			// clear any old output
			mOutputTextArea.setText("");

			// get the schema and queries from their respective textboxes
			String schema = mSchemaTextArea.getText();
			String query1 = mQuery1TextArea.getText();
			String query2 = mQuery2TextArea.getText();

			schema = ReadFile.readFileOrDie("sample_input/schema5.sql");
			query1 = ReadFile.readFileOrDie("sample_input/query5a.sql");
			query2 = ReadFile.readFileOrDie("sample_input/query5b.sql");

			// if (schema.equals("") || query1.equals("") || query2.equals(""))
			// return;

			// create a worker to test these user inputs
			mCurrentWorker = mQueryComparer.getCompareWorker(schema, query1,
					query2);

			// set the callback (PropertyChangeListener) for the worker
			mCurrentWorker.addPropertyChangeListener(mCompareListener);

			// start the worker (executes on a worker thread)
			mCurrentWorker.execute();

			// allow the user to click the cancel button
			mCancelButton.setEnabled(true);

			while (!mCurrentWorker.isDone()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			Vector<String> allTables = mQueryComparer.getAllTableNames();
			for (String t : mQueryComparer.getAllTuplesFromTable(allTables
					.lastElement()))
				System.out.println(t);
		}
	}

	/**
	 * This PropertyChangeListener get called when the Worker changes state
	 * (PENDING, STARTED, DONE).
	 */
	private class QueryCompareListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// when the worker is done, call handleQueryCompareCompletion()
			// to display results
			if (mCurrentWorker.getState() == StateValue.DONE) {
				handleWorkerCompletion();
			}
		}
	}

	/**
	 * This method when called the worker finishes (either due to finding an
	 * instance, or to user cancellation)
	 */
	private void handleWorkerCompletion() {
		// make sure the user cannot attempt to cancel again
		mCancelButton.setEnabled(false);
		ReturnValue rv = null;
		if (mCurrentWorker.isCancelled()) {
			// if the user cancelled, then there is nothing to show
			mOutputTextArea.setText("User cancelled");
		} else {
			// the user must not have cancelled,
			// so get the results from the worker
			try {
				rv = mCurrentWorker.get();
				// display the results to the user
				putReturnValueContentsInOutputWindow(rv);
			} catch (Exception e) {
				// this should not happen, but would likely be
				// due to a threading issue
				mOutputTextArea.setText("Error while getting results");
				mOutputTextArea.append(lineSeparator);
				StringWriter stringWriter = new StringWriter();
				e.printStackTrace(new PrintWriter(stringWriter));
				mOutputTextArea.append(stringWriter.toString());
			}
		}

		// allow the user to start the comparer again
		mStartButton.setEnabled(true);
	}

	/**
	 * This helper method puts the contents of a ReturnValue into the output
	 * window in the GUI.
	 * 
	 * @param rv
	 *            - The ReturnValue whose contents should be shown
	 */
	private void putReturnValueContentsInOutputWindow(ReturnValue rv) {
		// clear any old contents
		mOutputTextArea.setText("");
		// append the text message, if it has one
		if (rv.hasMessage()) {
			mOutputTextArea.append(rv.getMessage());
			mOutputTextArea.append(lineSeparator);
		}
		// append the stack trace of the exception, if it has one
		if (rv.hasException()) {
			Exception e = rv.getException();
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			mOutputTextArea.append(stringWriter.toString());
		}
	}

	/**
	 * When the user clicks the cancel button, this ActionListener is called
	 */
	private class CancelButtonActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// prevent the user from clicking false again
			mCancelButton.setEnabled(false);

			// tell the worker to stop trying to find an instance
			// (this will cause QueryCompareListener to be fired)
			mCurrentWorker.cancel(true);
		}
	}

	/**
	 * Changes the form's visibility
	 * 
	 * @param visible
	 *            - false for invisible
	 */
	public void setVisible(boolean visible) {
		mMonteCarloQueryForm.setVisible(visible);
	}

	class SyntaxHighlighter implements DocumentListener {
		private Set<String> keywords;
		private Style keywordStyle;
		private Style normalStyle;

		public SyntaxHighlighter(JTextPane editor) {
			keywordStyle = ((StyledDocument) editor.getDocument()).addStyle(
					"Keyword_Style", null);
			normalStyle = ((StyledDocument) editor.getDocument()).addStyle(
					"Keyword_Style", null);
			StyleConstants.setForeground(keywordStyle, Color.RED);
			StyleConstants.setForeground(normalStyle, Color.BLACK);
			keywords = new HashSet<String>();
			keywords.add("select");
			keywords.add("in");
			keywords.add("from");
			keywords.add("unique");
			keywords.add("create");
			keywords.add("exist");
			keywords.add("from");
			keywords.add("join");
			keywords.add("not");
			keywords.add("and");
			keywords.add("or");
			keywords.add("dec");

			keywords.add("on");
			keywords.add("union");
			keywords.add("where");
			keywords.add("null");
			keywords.add("column");
			keywords.add("distinct");
		}

		public void colouring(StyledDocument doc, int pos, int len)
				throws BadLocationException {
			int start = indexOfWordStart(doc, pos);
			int end = indexOfWordEnd(doc, pos + len);

			char ch;
			while (start < end) {
				ch = getCharAt(doc, start);
				if (Character.isLetter(ch) || ch == '_') {
					start = colouringWord(doc, start);
				} else {
					SwingUtilities.invokeLater(new ColouringTask(doc, start, 1,
							normalStyle));
					++start;
				}
			}
		}

		public int colouringWord(StyledDocument doc, int pos)
				throws BadLocationException {
			int wordEnd = indexOfWordEnd(doc, pos);
			String word = doc.getText(pos, wordEnd - pos);

			if (keywords.contains(word.toLowerCase())) {
				SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd
						- pos, keywordStyle));
			} else {
				SwingUtilities.invokeLater(new ColouringTask(doc, pos, wordEnd
						- pos, normalStyle));
			}

			return wordEnd;
		}

		public char getCharAt(Document doc, int pos)
				throws BadLocationException {
			return doc.getText(pos, 1).charAt(0);
		}

		public int indexOfWordStart(Document doc, int pos)
				throws BadLocationException {
			for (; pos > 0 && isWordCharacter(doc, pos - 1); --pos)
				;

			return pos;
		}

		public int indexOfWordEnd(Document doc, int pos)
				throws BadLocationException {
			for (; isWordCharacter(doc, pos); ++pos)
				;

			return pos;
		}

		public boolean isWordCharacter(Document doc, int pos)
				throws BadLocationException {
			char ch = getCharAt(doc, pos);
			if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') {
				return true;
			}
			return false;
		}

		@Override
		public void changedUpdate(DocumentEvent e) {

		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			try {
				colouring((StyledDocument) e.getDocument(), e.getOffset(),
						e.getLength());
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			try {
				colouring((StyledDocument) e.getDocument(), e.getOffset(), 0);
			} catch (BadLocationException e1) {
				e1.printStackTrace();
			}
		}

		private class ColouringTask implements Runnable {
			private StyledDocument doc;
			private Style style;
			private int pos;
			private int len;

			public ColouringTask(StyledDocument doc, int pos, int len,
					Style style) {
				this.doc = doc;
				this.pos = pos;
				this.len = len;
				this.style = style;
			}

			public void run() {
				try {
					doc.setCharacterAttributes(pos, len, style, true);
				} catch (Exception e) {
				}
			}
		}
	}
}
