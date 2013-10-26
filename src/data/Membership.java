
package data;

public class Membership {

	/* The user ID of the user belonging to a group */
	private String email;
	/* The confirmation token when requesting membership, empty if granted */
	private String token;
	
	public Membership() {}
	
	public Membership(String email, String token) {
		this.email = email;
		this.token = token;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
}
