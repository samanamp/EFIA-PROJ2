package handlers;

import org.lightcouch.NoDocumentException;

import data.UserData;

public class UserSession {

	private String email;
	private boolean valid;
	private String problem;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public String getProblem() {
		return problem;
	}

	public void setProblem(String problem) {
		this.problem = problem;
	}

	public UserSession() {
		email = "";
		valid = false;
		problem = "";
	}
	
	public UserSession(String email) {
		this.email = email;
		valid = false;
		problem = "";
	}
	
	/**
	 * 
	 * @param dbh
	 * @param email
	 * @param token
	 */
	public void verifySession(DBHandler dbh, String email, String token) {
		if (!UserData.isValidEmail(email)) {
			problem = "The e-mail provided is not valid.";
			return;
		}
		UserData user;
		try {
			user = dbh.getUser(email);
		} catch (NoDocumentException nde) {
			problem = "There is no user registered for the e-mail provided.";
			return;
		}
		
		if (token != null) {
			//Validate the user session
			if (user != null && user.getToken() != null) {
				if (!user.getToken().equals(token) || !user.isConfirmed()) {
					problem = "Invalid token";
					return;
				}
			} else {
				problem = "No session found";
				return;
			}
		} else {
			problem = "No access token provided";
			return;
		}
		
		valid = true;
		problem = "";
	}
}
