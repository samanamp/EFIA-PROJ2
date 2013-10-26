package handlers;

import java.io.IOException;
import java.util.ArrayList;

import data.Group;
import data.Membership;
import data.Message;

public class GroupHandler {
	DBHandler dbHandler;
	String localServerAddress;

	public GroupHandler(String localServerAddress) {
		dbHandler = new DBHandler();
		this.localServerAddress = localServerAddress;
	}

	// TODO Check for existing groupName for that owner.
	/**
	 * 
	 * @param groupName
	 * @param owner
	 * @return 2 if everything is OK
	 * @return 1 if group Exists
	 * @return -2 if any errors happened
	 */
	public void addNewGroup(String groupName, String owner) {
		Group group = new Group(groupName, owner);
		dbHandler.addNewGroup(group);
	}

	// TODO Implement the invitation token logic
	/**
	 * 
	 * @param groupID
	 * @param newUserEmail
	 * 
	 * @return 2 if everything is ok returns
	 * @return 1 if user registered and confirmed before
	 * @return 0 if there is no such user
	 * @return -1 if groupID is wrong
	 * @return -2 if any other errors happened
	 */
	public int addNewUserToGroup(String groupID, String newMemberEmail) {
		try {
			Membership newMember = new Membership(newMemberEmail,
					SecureGen.generateSecureString(32));
			if (!dbHandler.ifGroupExists(groupID))
				return -1;
			if(!dbHandler.ifUserExists(newMemberEmail))
				return 0;

			Group group = dbHandler.getGroup(groupID);
			ArrayList<Membership> members = group.getUsers();

			boolean userIsAMember = false;
			for (Membership member : members) {
				if (member.getEmail().equalsIgnoreCase(newMemberEmail)) {
					userIsAMember = true;
					newMember = member;
					if (member.getToken() == "")
						return 1;
				}
			}
			if (!userIsAMember)
				members.add(newMember);

			dbHandler.updateGroup(group);

			sendGroupMembershipConfirmationLink(group, newMember);
			return 2;
		} catch (Exception e) {
			return -2;
		}
	}

	private void sendGroupMembershipConfirmationLink(Group group,
			Membership member) throws IOException {
		String confirmMessage = "Please confirm your membership in \""
				+ group.getName() + "\" group owned by \"" + group.getOwner()
				+ "\"  by clicking on following link: \n" + "<a href=\"http://"
				+ localServerAddress + ":8080/proj2/Confirm?groupid="
				+ group.get_id() + "&token=" + member.getToken() + "&email="
				+ member.getEmail() + "\">Click me!</a>";
		System.out.println(confirmMessage);
		EmailHandler emailHandler = new EmailHandler(member.getEmail(),
				"Group Invitation", confirmMessage);
		emailHandler.start();
	}

	/**
	 * @retrun 2 if everything is ok
	 * @return 1 if confirmed before or token is wrong
	 * @return 0 if user is not registered to the group
	 * @return -1 if groupID is wrong
	 */
	public int confirmNewUser(String groupID, String userEmail, String token) {
		if (!dbHandler.ifGroupExists(groupID))
			return -1;
		Group group = dbHandler.getGroup(groupID);
		ArrayList<Membership> members = group.getUsers();

		for (Membership member : members) {
			if (member.getEmail().equalsIgnoreCase(userEmail)) {
				if (member.getToken().equalsIgnoreCase(token)){
					members.remove(member);
					member.setToken("");
					members.add(member);
					group.setUsers(members);
					dbHandler.updateGroup(group);
					return 2;
				}else
					return 1;
			}
		}
		return 0;
	}

	/**
	 * 
	 * @param groupID
	 * @param newMessage
	 * 
	 * @return 2 if all things are OK
	 * @return 0 if the user doesn't have the permission to post
	 */
	public int addNewMessageToGroup(String groupID, Message newMessage) {
		Group group = dbHandler.getGroup(groupID);
		boolean ifUserIsAMember = false;
		ArrayList <Membership> members = group.getUsers();
		for(Membership member:members){
			if(member.getEmail().equalsIgnoreCase(newMessage.getUser()))
				ifUserIsAMember=true;
		}
		
		if(!ifUserIsAMember)
			return 0;
		
		group.getMessages().add(newMessage);
		dbHandler.updateGroup(group);
		return 2;
	}

	/**
	 * 
	 * @param groupID
	 * @return
	 */
	public Group getGroup(String groupID) {
		return dbHandler.getGroup(groupID);
	}

	/**
	 * 
	 * @param groupID
	 * @return
	 */
	public ArrayList<Message> getMessagesOfGroup(String groupID) {
		return dbHandler.getGroup(groupID).getMessages();
	}

	/**
	 * 
	 * @param groupID
	 */
	public void removeGroup(String groupID) {
		dbHandler.deleteGroup(groupID);
	}

	public static void main(String[] args) {
		GroupHandler gh = new GroupHandler("127.0.0.1");
		//gh.addNewGroup("Samax", "saman.bonab@gmail.com");
		System.out.println(gh.addNewMessageToGroup("669caff1efad4a2bb2567a3682630758", new Message("cesarm@unimelb.edu.au", "hi everybody", 12346443)));
		//System.out.println(gh.confirmNewUser("669caff1efad4a2bb2567a3682630758", "samani", "o4kstpm4ec3cvc75lnhsui9g0fpa9tgo"));
		//System.out.println(gh.confirmNewUser("669caff1efad4a2bb2567a3682630758", "cesarm@unimelb.edu.au", "o4kstpm4ec3cvc75lnhsui9g0fpa9tgo"));
	}
}
