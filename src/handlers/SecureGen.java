package handlers;
import java.math.BigInteger;
import java.security.SecureRandom;


public class SecureGen {
	public static String generateSecureString(int stringLength){
		SecureRandom random = new SecureRandom();
		String token = new BigInteger(stringLength*5, random).toString(32);
		return token;
		
	}

}
