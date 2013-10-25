
package data;

public class Message {
	private String user;
	private String message;
	private long timestamp;
	
	public Message() {
		super();
	}
	
	/**
	 * 
	 * @param user
	 * @param message
	 * @param timestamp
	 */
	public Message(String user, String message, long timestamp) {
		super();
		this.user = user;
		this.message = message;
		this.timestamp = timestamp;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
