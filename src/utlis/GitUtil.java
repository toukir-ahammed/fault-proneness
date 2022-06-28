package utlis;

import java.io.IOException;

public class GitUtil {
	
	public static String[] getCommits(String base, String head, String repo) throws IOException {
		String commandString = "git -c core.abbrev=40 log --pretty=format:%h%x09%an%x09%ad%x09%s " + base + ".." + head ;
		String commitList = Command.excecute(commandString, repo);
		// System.out.println(commitList);
		return commitList.split("\n");
	}
	
	public static String[] getChanges(String commitHash, String repo) throws IOException {
		String commandString = "git diff --numstat " + commitHash + "^ " + commitHash + " --no-renames";
		String changedFilesString = Command.excecute(commandString, repo);
		return changedFilesString.split("\n");
	}

	public static String[] getChanges(String base, String head, String repo) throws IOException {
		String commandString = "git diff --numstat " + base + " " + head + " --no-renames";
		String changedFilesString = Command.excecute(commandString, repo);
		return changedFilesString.split("\n");
	}

}
