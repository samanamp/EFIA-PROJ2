package handlers;

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
		UserData user = dbh.getUser(email);
		if (token != null) {
			//Validate the user session
			if (user != null && user.getToken() != null) {
				if (!user.getToken().equals(token) || !user.isConfirmed()) {
					problem = "Invalid token";
				}
			} else {
				problem = "No session found";
			}
		} else {
			problem = "No access token provided";
		}
		
		valid = true;
		problem = "";
	}
}
