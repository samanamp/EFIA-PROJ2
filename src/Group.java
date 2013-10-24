import java.util.ArrayList;


public class Group {
	public String _id;
	public String _rev;
	public String owner;
	public ArrayList <String> users;
	public ArrayList <String> messages;
	
	public Group(String _id, String owner, ArrayList <String> users, ArrayList <String> messages){
		this._id = _id;
		this.owner = owner;
		this.users = users;
		this.messages = messages;		
	}
}
