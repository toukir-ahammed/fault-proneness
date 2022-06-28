package issue;

import java.util.HashMap;
import java.util.Map;

public abstract class IssueAnalysis {
	HashMap<String, Issue> issueMap;
	String issueDir;
	String cwd;
	String repo;
	
	public abstract void run(Map<String, Object> configmap);

}
