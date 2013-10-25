package handlers;

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Email {


	public void sendEmail(ArrayList<String> recipients, String fromAddress, String subject, String message)

			throws AddressException, MessagingException {

		if (message != null) {
			Properties props = new Properties();
			Session session = Session.getDefaultInstance(props, null);
			MimeMessage m = new MimeMessage(session);
			m.setFrom(new InternetAddress(fromAddress));
			for (int i = 0; i < recipients.size(); i++) {
				m.addRecipient(MimeMessage.RecipientType.TO,
						new InternetAddress(recipients.get(i)));
			}
			m.setSubject(subject);
			m.setContent(message, "text/html; charset=utf-8");
			Transport.send(m);
			
		} else 
			throw new NullPointerException("A message must be defined.");
	}

	public void sendEmail(String recipient, String fromAddress, String subject, String message)

			throws AddressException, MessagingException {

		ArrayList<String> res = new ArrayList<String>();
		res.add(recipient);
		sendEmail(res, fromAddress, subject, message);
	}
}
