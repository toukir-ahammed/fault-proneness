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

public class JiraAnalysis extends IssueAnalysis {
	private HashMap<String, HashMap<String, Integer>> df;

	public JiraAnalysis(String repo, String cwd, String issueDir) {
		this.repo = repo;
		this.cwd = cwd;
		this.issueDir = issueDir;
		this.issueMap = new HashMap<>();
		this.df = new HashMap<>();
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
			// Issue(String bugId, String type, String status, String resolution)
			issueMap.put(issueInfo[1], new Issue(issueInfo[1], issueInfo[0], issueInfo[6], issueInfo[7]));
		}
	}

	private void analyse(String base, String head, String bugPattern, String project) throws IOException {

		// getAllissues(issueDir);
		System.out.println("Issues: " + issueMap.size());

		String[] commits = GitUtil.getCommits(base, head, repo);
		System.out.println("Commits: " + commits.length);

		StringBuilder stringBuilder = new StringBuilder();
		// stringBuilder.append("cycle\tcommit\tbugid\tadded\tdeleted\tfile\n");
		stringBuilder.append("Project,Revision(prev),Revision(cur),Commit,BugId,Added,Deleted,File, Total\n");

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
				String bugId = commitMessage.substring(matcher.start(), matcher.end());
				// System.out.println(bugId + " - " + commitMessage);
				if (issueMap.containsKey(bugId)) {
					Issue issue = issueMap.get(bugId);
					if (issue.type.equals("Bug") && (issue.status.equals("Closed") || issue.status.equals("Resolved"))
							&& issue.resolution.equals("Fixed")) {
//						System.out.println(commitHash + "\t" + commitMessage + "\t" + bugId + "\t" + issue.status + "\t"
//								+ issue.resolution);
						String[] changes = GitUtil.getChanges(commitHash, repo);
						for (String change : changes) {
							
							if(change.split("\t").length < 3) continue;
							
							String added = change.split("\t")[0];
							String deleted = change.split("\t")[1];
							String file = change.split("\t")[2];
							
							int total = 0;
							
							try {
								total = Integer.parseInt(added) + Integer.parseInt(deleted);				
							} catch (NumberFormatException e) {
								// TODO: handle exception
								System.err.println(e);
								System.err.println("Ignoring the following change:");
								System.err.println(change);
								continue;
							}
							
							stringBuilder.append(project + ',' +base + ',' + head + ',' + commitHash + ',' + bugId + ',' + change.replace('\t', ',') + "," + total);
							stringBuilder.append(System.lineSeparator());
							
							if (df.containsKey(file)) {
								HashMap<String, Integer> tempMap = df.get(file);
								if(tempMap.containsKey(head)) {
									tempMap.put(head, tempMap.get(head)+total);
								}
								else {
									tempMap.put(head, total);
								}
								
								df.put(file, tempMap);
							} else {
								HashMap<String, Integer> tempMap = new HashMap<>();
								tempMap.put(head, total);
								df.put(file, tempMap);
							}
						}

					}
				}

			}

		}
		
		
		
		MyFileUtils.writeToFile(new File(cwd + File.separator + "fixing-changes-" + base.replace('/', '-')  + "--" + head.replace('/', '-')  + ".csv"),
				stringBuilder.toString());

	}

	public void run(Map<String, Object> configmap) {
		// TODO Auto-generated method stub
		@SuppressWarnings("unchecked")
		ArrayList<String> revisions = (ArrayList<String>) configmap.get("revisions");
		String bugPattern = (String) configmap.get("bugpattern");
		String project = (String) configmap.get("project");
		try {
			getAllissues(issueDir);

			for (int i = 0; i < revisions.size() - 1; i++) {
				String base = (String) revisions.get(i);
				String head = (String) revisions.get(i + 1);
//				String bugPattern = "Bug.\\d+";
				System.out.println("Analysing " + base + ".." + head);
				analyse(base, head, bugPattern, project);
			}
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("Class");
			
			for (int i = 1; i < revisions.size(); i++) {
				stringBuilder.append(",");
				stringBuilder.append(revisions.get(i));
			}
			
			stringBuilder.append(System.lineSeparator());
			
			for (Map.Entry<String, HashMap<String, Integer>> mapElement : df.entrySet()) {
				
				String file = mapElement.getKey();
				
				stringBuilder.append(file);
				
				HashMap<String, Integer> tempMap = mapElement.getValue();
				
				for (int i = 1; i < revisions.size(); i++) {
					String string = revisions.get(i);
					if (tempMap.containsKey(string)) {
						stringBuilder.append(",");
						stringBuilder.append(tempMap.get(string));
						
					} else {
						stringBuilder.append(",");
						stringBuilder.append(0);
					}
				}
				
				stringBuilder.append(System.lineSeparator());
				
			}
			
			MyFileUtils.writeToFile(new File(cwd + File.separator + project + "-fp" + ".csv"), stringBuilder.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
