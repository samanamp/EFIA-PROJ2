
package data;

import java.util.ArrayList;


public class Group {
	
	private String _id;
	private String _rev;
	/* The _id can't be the name, since the name is unique with respect to the owner, 
	 * meaning different owners can have groups with the same name. */
	private String name;
	private String owner;
	private ArrayList <Membership> users= new ArrayList<Membership>();
	/* Messages have to contain different attributes, not just the text included the 
	 * timestamp in milliseconds. */
	private ArrayList <Message> messages= new ArrayList<Message>();
	
	public Group(String name, String owner, ArrayList <Membership> users, ArrayList <Message> messages){
		this.name = name;
		this.owner = owner;
		this.users = users;
		this.messages = messages;		
	}

	public Group(String name, String owner) {
		this.name = name;
		this.owner = owner;
		users.add(new Membership(owner, ""));
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String get_rev() {
		return _rev;
	}

	public void set_rev(String _rev) {
		this._rev = _rev;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public ArrayList<Membership> getUsers() {
		return users;
	}

	public void setUsers(ArrayList<Membership> users) {
		this.users = users;
	}

	public ArrayList<Message> getMessages() {
		return messages;
	}

	public void setMessages(ArrayList<Message> messages) {
		this.messages = messages;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Group))
			return false;
		Group other = (Group) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		return true;
	}
	
	public String toString() {
		return "Group(" + name + "," + owner + ")";
	}
}
