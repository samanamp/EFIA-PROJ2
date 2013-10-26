
package handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.lightcouch.CouchDbClient;
import org.lightcouch.DocumentConflictException;
import org.lightcouch.NoDocumentException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import data.Error;
import data.Group;
import data.UserData;

public class DBHandler {

	private CouchDbClient dbClient;
	private Gson gson;

	public DBHandler() {
		dbClient = new CouchDbClient("proj2", true, "http", "localhost", 5984,
				null, null);
		gson = new Gson();
	}

	/**
	 * Deletes the user data related to the given e-mail.
	 * 
	 * @param The
	 *            e-mail the user is registered with
	 * @throws NoDocumentException
	 *             If the user defined by the e-mail does not exist.
	 * @throws DocumentConflictException
	 *             If there was a conflict while deleting the object.
	 */
	public void deleteUser(String userEmail) throws DocumentConflictException,
			NoDocumentException {

		UserData user = this.getUser(userEmail);

		String jsonString = gson.toJson(user);
		JsonObject jsonobj = dbClient.getGson().fromJson(jsonString,
				JsonObject.class);
		dbClient.remove(jsonobj);
	}

	/**
	 * Writes errors into the database
	 * 
	 * @param type
	 *            The type of the error, which is the type of the Exception
	 * @param t
	 *            The exception itself.
	 * @throws DocumentConflictException
	 *             If there was a conflict while updating the object.
	 */
	public void writeError(String type, Throwable t)
			throws DocumentConflictException {
		Error error = new Error();
		error.setType(type);
		error.setMessage(t.getMessage());
		error.setDetail(getStackTrace(t));

		addNewError(error);
	}

	public UserData getUser(String email) {
		UserData ud = dbClient.find(UserData.class, email);

		return ud;
	}

	public boolean saveToken(String email, String token) {
		UserData ud = dbClient.find(UserData.class, email);
		ud.setToken(token);
		dbClient.update(ud);

		ud = dbClient.find(UserData.class, email);
		if (ud.getToken().equals(token)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean removeToken(String email, String token) {
		UserData ud = dbClient.find(UserData.class, email);
		if (ud.getToken().equals(token)) {
			ud.setToken(null);
			dbClient.update(ud);
		}

		ud = dbClient.find(UserData.class, email);

		if (ud.getToken() == null) {
			return true;
		} else {
			return false;
		}
	}

	public void addNewUser(UserData newUser) {
		gson = new Gson();
		String jsonString = gson.toJson(newUser);
		JsonObject jsonobj = dbClient.getGson().fromJson(jsonString,
				JsonObject.class);
		jsonobj.addProperty("_id", newUser.getEmail());
		dbClient.save(jsonobj);
	}

	public void addNewError(Error error) {
		gson = new Gson();
		String jsonString = gson.toJson(error);
		JsonObject jsonobj = dbClient.getGson().fromJson(jsonString,
				JsonObject.class);
		dbClient.save(jsonobj);
	}

	/* To transform the Stack Trace into a String */
	private String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);

		return sw.toString();
	}

	public boolean ifUserExists(String email) {
		return dbClient.contains(email);
	}

	public void updateUser(UserData userObject) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(userObject);
		JsonObject jsonobj = dbClient.getGson().fromJson(jsonString,
				JsonObject.class);
		dbClient.update(jsonobj);
	}

	public void closeConnection() {
		dbClient.shutdown();
	}

	// --------GROUPS--------------------------------------------
	public void addNewGroup(Group group) {
		gson = new Gson();
		String jsonString = gson.toJson(group);
		JsonObject jsonobj = dbClient.getGson().fromJson(jsonString,
				JsonObject.class);
		dbClient.save(jsonobj);
	}
	
	public boolean ifGroupExists(String groupID){
		return dbClient.contains(groupID);
	}
	public Group getGroup(String groupID) {
		Group group = dbClient.find(Group.class, groupID);
		return group;
	}
	
	public ArrayList<Group> getGroups(String userID) {
		List<Group> list = dbClient.view("groups/by_user")
			 	.key(userID).includeDocs(true).query(Group.class);
		ArrayList<Group> res = new ArrayList<Group>(list);
		return res;
	}
	
	public ArrayList<Group> getGroupsByOwner(String userID) {
		List<Group> list = dbClient.view("groups/by_owner")
			 	.key(userID).includeDocs(true).query(Group.class);
		ArrayList<Group> res = new ArrayList<Group>(list);
		return res;
	}

	// TO-DO: CHECK FOR CONCURRENCY
	/* NOTE: Concurrency is already taken care of because of the synchronized way
	 * of implementing the logic in the Servlet. Since only one instance of the 
	 * Servlet exists per deployment, this logic can only be called by the only Thread
	 * belonging to the Servlet. */
	public void updateGroup(Group group) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(group);
		JsonObject jsonobj = dbClient.getGson().fromJson(jsonString,
				JsonObject.class);
		dbClient.update(jsonobj);
	}

	public void deleteGroup(String groupID) throws DocumentConflictException,
			NoDocumentException {

		Group group = this.getGroup(groupID);

		String jsonString = gson.toJson(group);
		JsonObject jsonobj = dbClient.getGson().fromJson(jsonString,
				JsonObject.class);
		dbClient.remove(jsonobj);
	}
}