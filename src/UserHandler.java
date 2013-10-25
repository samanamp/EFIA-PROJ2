import java.io.IOException;

public class UserHandler {

	private DBHandler dbHandler;
	private String localServerAddress;

	public UserHandler(String localServerAddress) {
		dbHandler = new DBHandler();
	}
	
	/* returns 2 if everything is ok 
	 * returns 1 if user registered before
	 * returns -2 for every other error
	 * ignores if the user registered before
	 * */
	public int registerUser(String email){
		try{
		if (!dbHandler.ifUserExists(email)){
			UserData user = new UserData();
			user.setEmail(email);
			
			String token = SecureGen.generateSecureString(32);
			user.setToken(token);
			dbHandler.addNewUser(user);
			return 2;
		}}catch(Exception e){
			return -2;
		}
		return 1;
	}

	/*
	 * returns 2 if everything is ok returns 1 if user confirmed it before
	 * returns 0 if user has requested it less than 5 minuts back returns -1 if
	 * userDoesn't exist returns -2 with anyother error
	 */
	public int confirmEmail(String email) {
		try {
			if (!dbHandler.ifUserExists(email))
				return -1;

			UserData user = dbHandler.getUser(email);

			if (user.isConfirmed())
				return 1;

			long remain = System.currentTimeMillis()
					- user.getConfirmationTimestamp();
			remain = remain / 1000; // Converting to seconds

			if (remain < (5 * 60))
				return 0;

			sendConfirmationEmail(user);
			user.setConfirmationTimestamp(System.currentTimeMillis());
			dbHandler.updateUser(user);
			return 2;
		} catch (Exception e) {
			return -2;
		}

	}

	private void sendConfirmationEmail(UserData newUser) throws IOException {
		String confirmMessage = "Please confirm your registration by clicking on following link: \n"
				+ "<a href=\"http://"
				+ localServerAddress
				+ ":8080/proj1/Confirm?token="
				+ newUser.getToken()
				+ "&email="
				+ newUser.getEmail() + "\">Click me!</a>";
		EmailHandler emailHandler = new EmailHandler(newUser.getEmail(),
				"Account Confirmation", confirmMessage);
		emailHandler.start();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
