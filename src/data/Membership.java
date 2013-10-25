
package data;

public class Membership {

	/* The user ID of the user belonging to a group */
	private String user;
	/* The confirmation token when requesting membership, empty if granted */
	private String token;
	
	public Membership() {}
	
	public Membership(String user, String token) {
		this.user = user;
		this.token = token;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
}
