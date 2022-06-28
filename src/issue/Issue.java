package issue;

public class Issue {
	public String bugId;
	public String type;
	public String status;
	public String resolution;
	public Issue(String bugId, String type, String status, String resolution) {
		super();
		this.bugId = bugId;
		this.type = type;
		this.status = status;
		this.resolution = resolution;
	}	

}
