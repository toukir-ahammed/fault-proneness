package utlis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileUtils {
	public static void writeToFile(File file, String content) {
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	public static String readFile(File filename) throws IOException {
		BufferedReader bReader = new BufferedReader(new FileReader(filename));
		
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		line = bReader.readLine();
		while( (line = bReader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(System.lineSeparator());
		}
		stringBuilder.deleteCharAt(stringBuilder.length()-1);
		bReader.close();
		return stringBuilder.toString();
	}
}
