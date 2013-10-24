import java.util.ArrayList;


public class Group {
	public String _id;
	public String _rev;
	public String owner;
	public ArrayList <String> users= new ArrayList<String>();
	public ArrayList <String> messages= new ArrayList<String>();
	
	public Group(String _id, String owner, ArrayList <String> users, ArrayList <String> messages){
		this._id = _id;
		this.owner = owner;
		this.users = users;
		this.messages = messages;		
	}

	public Group(String _id, String owner) {
		this._id = _id;
		this.owner = owner;
		users.add(owner);
	}
}
