package edu.rutgers.cs541;

import java.util.Vector;

public class Strategy {
	// all doubles are assigned with integers
	Vector<Integer> maxInt;
	Vector<Integer> maxLength;
	Vector<String> candidateCharacter;
	Vector<Double> nullProbability;
	int index;

	Strategy() {
		index = 2;

		maxInt = new Vector<Integer>();
		maxInt.add(2);
		maxInt.add(2);
		maxInt.add(200);
		maxInt.add(200);

		maxLength = new Vector<Integer>();
		maxLength.add(1);
		maxLength.add(1);
		maxLength.add(1);
		maxLength.add(1);

		candidateCharacter = new Vector<String>();
		candidateCharacter
				.add("QWERTYUIOP[]ASDFGHJKL;XCVBNM,./1234567890-=abcdefghi");
		candidateCharacter.add("a0");
		candidateCharacter.add("aB");
		candidateCharacter
				.add("QWERTYUIOP[]ASDFGHJKL;XCVBNM,./1234567890-=abcdefghi");

		nullProbability = new Vector<Double>();
		nullProbability.add(0.2);
		nullProbability.add(0.2);
		nullProbability.add(0.2);
		nullProbability.add(0.2);
	}

	void changeIndex() {
		index = (index + 1) % maxInt.size();
	}

	public int getMaxInt() {
		return maxInt.elementAt(index);
	}

	public int getMaxLength() {
		return maxLength.elementAt(index);
	}

	public String getCandidateCharacter() {
		return candidateCharacter.elementAt(index);
	}

	public double getNullProbability() {
		return nullProbability.elementAt(index);
	}
}
