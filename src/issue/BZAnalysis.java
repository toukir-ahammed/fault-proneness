package issue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utlis.GitUtil;
import utlis.MyFileUtils;

public class BZAnalysis extends IssueAnalysis {

	public BZAnalysis(String repo, String cwd, String issueDir) {
		this.repo = repo;
		this.cwd = cwd;
		this.issueDir = issueDir;
		this.issueMap = new HashMap<>();
	}

	private void getAllissues(String dir) throws IOException {
		File folder = new File(dir);
		File[] listOfFiles = folder.listFiles();

		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < listOfFiles.length; i++) {
			System.out.println(listOfFiles[i]);
			String content = MyFileUtils.readFile(listOfFiles[i]);
			stringBuilder.append(content);
			stringBuilder.append(System.lineSeparator());
		}

		stringBuilder.deleteCharAt(stringBuilder.length() - 1);

		String[] issueLines = stringBuilder.toString().split("\n");

		for (String issueLine : issueLines) {
			String[] issueInfo = issueLine.split(",");
			// System.out.println(issueInfo[1] + "\t" + issueInfo[0] + "\t" + issueInfo[7] +
			// " \t" + issueInfo[8]);
			issueMap.put(issueInfo[0].replace("\"", ""), new Issue(issueInfo[0].replace("\"", ""),
					issueInfo[8].replace("\"", ""), issueInfo[4].replace("\"", ""), issueInfo[5].replace("\"", "")));
		}
	}

	private void analyse(String base, String head, String bugPattern) throws IOException {

		System.out.println("Issues: " + issueMap.size());

		String[] commits = GitUtil.getCommits(base, head, repo);
		System.out.println("Commits: " + commits.length);

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("cycle\tcommit\tbugid\tadded\tdeleted\tfile\n");

		for (String commit : commits) {
			String[] commitInfo = commit.split("\t");

			String commitHash = commitInfo[0];
			if (commitHash.equals("")) {
				continue;
			}
			String commitMessage = commitInfo[3];

			// System.out.println(commitHash + "\t" + commitMessage);
			// System.out.println(commit);
			// String bugPattern = "JENA-\\d+";
			Pattern pattern = Pattern.compile(bugPattern, Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(commitMessage);

			if (matcher.find()) {
				// matcher.start()+3 for Bugzilla
				String bugId = commitMessage.substring(matcher.start() + 4, matcher.end());
				if (issueMap.containsKey(bugId)) {
					Issue issue = issueMap.get(bugId);
					if (!issue.type.equals("enhancement") && issue.status.equals("RESOLVED")
							&& issue.resolution.equals("FIXED")) {
						System.out.println(commitHash + "\t" + commitMessage + "\t" + bugId + "\t" + issue.status + "\t"
								+ issue.resolution);
						String[] changes = GitUtil.getChanges(commitHash, repo);
						for (String change : changes) {
							stringBuilder.append(base + "-" + head + "\t" + commitHash + "\t" + bugId + "\t" + change);
							stringBuilder.append(System.lineSeparator());
						}

					}
				}

			}

		}
		MyFileUtils.writeToFile(new File(cwd + File.separator + "fixing-changes-" + base  + "--" + head + ".csv"),
				stringBuilder.toString());

	}

	public void run(Map<String, Object> configmap) {
		// TODO Auto-generated method stub
		
		@SuppressWarnings("unchecked")
		ArrayList<String> revisions = (ArrayList<String>) configmap.get("revisions");
		String bugPattern = (String) configmap.get("bugpattern");
		try {
			getAllissues(issueDir);
			
			for (int i = 0; i < revisions.size()-1; i++) {
				String base = (String) revisions.get(i);
				String head = (String) revisions.get(i+1);
//				String bugPattern = "Bug.\\d+";
				System.out.println("Analysing " + base + ".." + head);
				analyse(base, head, bugPattern);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
