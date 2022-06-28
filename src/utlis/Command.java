package utlis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Command {
	public static String printResults(Process process) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line = "";
		String output = "";
		while ((line = reader.readLine()) != null) {
			// System.out.println(line);
			output += line + "\n";
		}
		// System.out.println(output);
		return output;
	}

	public static String excecute(String command, String dir) throws IOException {
		Process process = Runtime.getRuntime().exec(command, null, new File(dir));
		String output = printResults(process);
		process.destroy();
		return output;
	}

}
