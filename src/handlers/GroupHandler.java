package handlers;

import java.io.IOException;
import java.util.ArrayList;

import data.Group;
import data.Membership;
import data.Message;
import exceptions.CustomException;

public class GroupHandler {
	DBHandler dbHandler;
	String localServerAddress;

	public GroupHandler(String localServerAddress) {
		dbHandler = new DBHandler();
		this.localServerAddress = localServerAddress;
	}

	/**
	 * @param groupName
	 * @param owner
	 * @throw CustomException In case the group name is repeated for that owner
	 */
	public void addNewGroup(String groupName, String owner) 
			throws CustomException {
		Group group = new Group(groupName, owner);
		ArrayList<Group> groups = dbHandler.getGroupsByOwner(owner);
		if (groups.contains(group)) {
			throw new CustomException(CustomException.GROUP_NAME_UNAVAILABLE);
		}
		dbHandler.addNewGroup(group);
	}
	
	/**
	 * 
	 * @param email
	 */
	public ArrayList<Group> getGroups(String email) {
		return dbHandler.getGroups(email);
	}

	// TODO Implement the invitation token logic
	/**
	 * 
	 * @param groupID
	 * @param newUserEmail
	 * 
	 */
	public void addNewUserToGroup(String groupID, String newMemberEmail) throws CustomException {
		try {
			Membership newMember = new Membership(newMemberEmail,
					SecureGen.generateSecureString(32));
			if (!dbHandler.ifGroupExists(groupID))
				throw new CustomException("Group", "The group ID is wrong");
			if(!dbHandler.ifUserExists(newMemberEmail))
				throw new CustomException("Group", "There is no such user");

			Group group = dbHandler.getGroup(groupID);
			ArrayList<Membership> members = group.getUsers();

			boolean userIsAMember = false;
			for (Membership member : members) {
				if (member.getEmail().equalsIgnoreCase(newMemberEmail)) {
					userIsAMember = true;
					newMember = member;
					if (member.getToken() == "")
						throw new CustomException("Group", "The member has already confirmed");
				}
			}
			if (!userIsAMember)
				members.add(newMember);

			dbHandler.updateGroup(group);

			sendGroupMembershipConfirmationLink(group, newMember);
		} catch (Exception e) {
			throw new CustomException(CustomException.INTERNAL_ERROR);
		}
	}

	private void sendGroupMembershipConfirmationLink(Group group,
			Membership member) throws IOException {
		String confirmMessage = "Please confirm your membership in \""
				+ group.getName() + "\" group owned by \"" + group.getOwner()
				+ "\"  by clicking on following link: \n" + "<a href=\"http://"
				+ localServerAddress + ":8080/proj2/GroupServlet?method=confirm&groupid="
				+ group.get_id() + "&token=" + member.getToken() + "&email="
				+ member.getEmail() + "\">Click me!</a>";
		System.out.println(confirmMessage);
		EmailHandler emailHandler = new EmailHandler(member.getEmail(),
				"Group Invitation", confirmMessage);
		emailHandler.start();
	}

	/**
	 * @throws CustomException 
	 */
	public int confirmNewUser(String groupID, String userEmail, String token) throws CustomException {
		if (!dbHandler.ifGroupExists(groupID))
			throw new CustomException("Group", "The group ID is wrong");
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
				}else
					throw new CustomException("Group", "Either confirmed before or the token is wrong");
			}
		}
		throw new CustomException("Group", "The user hasn't been invited to the group");
	}

	/**
	 * 
	 * @param groupID
	 * @param newMessage
	 * @throws CustomException 
	 */
	public void addNewMessageToGroup(String groupID, Message newMessage) throws CustomException {
		Group group = dbHandler.getGroup(groupID);
		boolean ifUserIsAMember = false;
		ArrayList <Membership> members = group.getUsers();
		for(Membership member:members){
			if(member.getEmail().equalsIgnoreCase(newMessage.getUser()))
				ifUserIsAMember=true;
		}
		
		if(!ifUserIsAMember)
			throw new CustomException("Group", "The user doesn't have permission to post");
		
		group.getMessages().add(newMessage);
		dbHandler.updateGroup(group);
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
		//GroupHandler gh = new GroupHandler("127.0.0.1");
		//gh.addNewGroup("Samax", "saman.bonab@gmail.com");
		//System.out.println(gh.addNewMessageToGroup("669caff1efad4a2bb2567a3682630758", new Message("cesarm@unimelb.edu.au", "hi everybody", 12346443)));
		//System.out.println(gh.confirmNewUser("669caff1efad4a2bb2567a3682630758", "samani", "o4kstpm4ec3cvc75lnhsui9g0fpa9tgo"));
		//System.out.println(gh.confirmNewUser("669caff1efad4a2bb2567a3682630758", "cesarm@unimelb.edu.au", "o4kstpm4ec3cvc75lnhsui9g0fpa9tgo"));

	}
}
