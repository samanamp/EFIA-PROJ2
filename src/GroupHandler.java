import java.util.ArrayList;


public class GroupHandler {
	DBHandler dbHandler;
	
	public GroupHandler(){
		dbHandler = new DBHandler();
	}
	
	public void addNewGroup(String groupName, String owner){
		Group group = new Group(groupName, owner);
		dbHandler.addNewGroup(group);		
	}
	
	public void addNewUserToGroup(String groupName, String newUserEmail){
		Group group = dbHandler.getGroup(groupName);
		group.users.add(newUserEmail);
		dbHandler.updateGroup(group);
	}
	
	public void addNewMessageToGroup(String groupName, String newMessage){
		Group group = dbHandler.getGroup(groupName);
		group.messages.add(newMessage);
		dbHandler.updateGroup(group);
	}
	
	public Group getGroup(String groupName){
		return dbHandler.getGroup(groupName);
	}
	
	public ArrayList <String> getMessagesOfGroup(String groupName){
		return dbHandler.getGroup(groupName).messages;
	}
	
	public void removeGroup(String groupName){
		dbHandler.deleteGroup(groupName);
		
	}
	
	
	public static void main(String[] args) {
		GroupHandler gh = new GroupHandler();
		gh.addNewGroup("newGroupHandler", "everybody");
		gh.addNewUserToGroup("newGroupHandler", "MySelf");
		gh.addNewMessageToGroup("newGroupHandler", "Samn Aleyk");		

	}

}
