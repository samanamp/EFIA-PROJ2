package handlers;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.lightcouch.NoDocumentException;

import data.UserData;

public class SendPassword {
	public static void sendNewPasswordForUser(UserData udata)
			throws AddressException, MessagingException, NoDocumentException,
			IllegalArgumentException {

		// set new password
		String email = udata.getEmail();
		String password = SecureGen.generateSecureString(12);
		udata.setPassword(password);

		// Send email address
		String emailMessage = "The password for your proj2 account is: "
				+ password + "<br /> Keep it secret, keep it safe.";
		EmailHandler emailHandler = new EmailHandler(email, "Account Password",
				emailMessage);
		emailHandler.start();

	}

	public static void sendReminder(UserData userData, long timeBetweenTries)
			throws Exception {
		// if less than 5 minutes passes from last confirmation?
		if ((System.currentTimeMillis() - userData.getReminderTimestamp()) < timeBetweenTries)
			throw new Exception(
					"You have requested it less than 5 minutes ago! Try Again later");
		// send password
		EmailHandler emailHandler = new EmailHandler(userData.getEmail(),
				"Email Reminder",
				"You have requested a password reminder, your password is:\n"
						+ userData.getPassword());
		emailHandler.start();
	}
}
