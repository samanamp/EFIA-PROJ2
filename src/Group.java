
public class Group {
	public String _id;
	public String _rev;
	public String owner;
	public String [] users;
	public String [] messages;
	
	public Group(String _id, String owner, String [] users, String [] messages){
		this._id = _id;
		this.owner = owner;
		this.users = users;
		this.messages = messages;		
	}
}
