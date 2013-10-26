package exceptions;

public class CustomException extends Exception {

	private static final long serialVersionUID = 3929045928223848376L;
	private String type;
	
	public static final String GROUP_NAME_UNAVAILABLE = "GroupNameUnavailable";
	public static final String INTERNAL_ERROR = "Internal Error";
	
	public CustomException(String type) {
		super();
		this.type = type;
	}
	
	public CustomException(String type, String message) {
		super(message);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
