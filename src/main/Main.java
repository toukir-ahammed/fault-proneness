package main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import issue.BZAnalysis;
import issue.IssueAnalysis;
import issue.JiraAnalysis;

public class Main {
	public static void main(String[] args) {
		
		String cwd = "demo-project";
		String project = "activemq";

		String projectConfigFile = cwd + File.separator + project + ".conf";
		String repo = cwd + File.separator + project + "-repo";
		String resDir = cwd + File.separator + "res" + File.separator + project;
		String issueDir = cwd + File.separator + project + "-issues";

		File dir = new File(resDir);
		if (!dir.exists())
			dir.mkdirs();

		try {
			Yaml projectConfigYaml = new Yaml();
			Reader projectFileReader = new FileReader(projectConfigFile);
			Map<String, Object> configmap = projectConfigYaml.load(projectFileReader);
			String issueTracker = (String) configmap.get("issuetracker");
			System.out.println(configmap.get("revisions"));

			dir = new File(resDir + File.separator);
			if (!dir.exists())
				dir.mkdir();

			IssueAnalysis analysis = null;
			
			if (issueTracker.equals("bugzilla")) {
				analysis = new BZAnalysis(repo, dir.getAbsolutePath(), issueDir);
			} else if (issueTracker.equals("jira")){
				analysis = new JiraAnalysis(repo, dir.getAbsolutePath(), issueDir);
			}
			
			analysis.run(configmap);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
	}
}
