package edu.rutgers.cs541;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class ReadFile {
	public static String readFileOrDie(String fileName) {
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
