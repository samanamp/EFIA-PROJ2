package handlers;

import java.io.IOException;

import data.UserData;
import exceptions.CustomException;

public class UserHandler {

	private DBHandler dbHandler;
	private String localServerAddress;

	public UserHandler(String localServerAddress) {
		dbHandler = new DBHandler();
	}

	public boolean registerUser(String email) throws CustomException {
		try {
			if (!dbHandler.ifUserExists(email)) {
				UserData user = new UserData();
				user.setEmail(email);

				String token = SecureGen.generateSecureString(32);
				user.setToken(token);
				dbHandler.addNewUser(user);
				
			}
		} catch (Exception e) {
			throw new CustomException(CustomException.INTERNAL_ERROR, "Internal Error");
		}
		return true;
	}


	public boolean sendConfirmation(String email) throws CustomException {
		try {
			if (!dbHandler.ifUserExists(email))
				throw new CustomException("User", "The user doesn't exist");

			UserData user = dbHandler.getUser(email);

			if (user.isConfirmed())
				throw new CustomException("User", "The user has confirmed before");

			long remain = System.currentTimeMillis()
					- user.getConfirmationTimestamp();
			remain = remain / 1000; // Converting to seconds

			if (remain < (5 * 60))
				throw new CustomException("User", "The user has requested it less than 5 minutes back");

			sendConfirmationEmail(user);
			user.setConfirmationTimestamp(System.currentTimeMillis());
			dbHandler.updateUser(user);
			return true;
		} catch (Exception e) {
			throw new CustomException(CustomException.INTERNAL_ERROR);
		}

	}

	private void sendConfirmationEmail(UserData newUser) throws IOException {
		String confirmMessage = "Please confirm your registration by clicking on following link: \n"
				+ "<a href=\"http://"
				+ localServerAddress
				+ ":8080/proj2/Confirm?token="
				+ newUser.getToken()
				+ "&email="
				+ newUser.getEmail() + "\">Click me!</a>";
		EmailHandler emailHandler = new EmailHandler(newUser.getEmail(),
				"Account Confirmation", confirmMessage);
		emailHandler.start();
	}


	public boolean confirm(String email, String token) throws CustomException {
		try {
			if (!dbHandler.ifUserExists(email))
				throw new CustomException("User", "The user doesn't exist");

			UserData userData = dbHandler.getUser(email);

			if (userData.isConfirmed())
				throw new CustomException("User", "The user has confirmed before");

			if (!userData.getToken().equalsIgnoreCase(token))
				throw new CustomException("User", "The email and token doesn't match");

			userData.setConfirmed(true);
			SendPassword.sendNewPasswordForUser(userData);
			dbHandler.updateUser(userData);
			return true;
		} catch (Exception e) {
			throw new CustomException(CustomException.INTERNAL_ERROR);
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
