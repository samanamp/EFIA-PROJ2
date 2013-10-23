import java.io.PrintWriter;
import java.io.StringWriter;
import org.lightcouch.CouchDbClient;
import org.lightcouch.DocumentConflictException;
import org.lightcouch.NoDocumentException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class DBHandler {

	private CouchDbClient dbClient;
	private Gson gson;

	public DBHandler() {
		dbClient = new CouchDbClient("proj1", true, "http", "localhost", 5984,
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

	public void updateObject(UserData userObject) {
		Gson gson = new Gson();
		String jsonString = gson.toJson(userObject);
		JsonObject jsonobj = dbClient.getGson().fromJson(jsonString,
				JsonObject.class);
		dbClient.update(jsonobj);
	}

	public void closeConnection() {
		dbClient.shutdown();
	}
	
	public static void main(String [] args){
		
	}

}