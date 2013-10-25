package servlets;
import handlers.DBHandler;
import handlers.SecureGen;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.lightcouch.CouchDbException;

import data.UserData;

/**
 * Servlet implementation class Chat
 */
@WebServlet("/Login")
public class Login extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Login() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		// create a JSON object (using json-simple)
		JSONObject res = executeSynchronizedLogic(request);

		// respond to the web browser
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(res);
		out.close();
	}

	@SuppressWarnings("unchecked")
	public synchronized JSONObject executeSynchronizedLogic(
			HttpServletRequest request) {

		JSONObject res = new JSONObject();
		String errorMessage = null;
		String errorType = null;
		boolean success = true;
		UserData ud = null;
		DBHandler dbh = null;
		try {
			try {
				dbh = new DBHandler();
			} catch (Exception e){}
			if (dbh == null)
				throw new CouchDbException(
						"Cannot connect to DB, please try again later!");

			String token = null;
			String email = request.getParameter("email");
			String password = request.getParameter("password");

			boolean isPasswordValid = password != null && !password.equals("");

			if (!UserData.isValidEmail(email) || !isPasswordValid)
				throw new IllegalArgumentException(
						"Please provide a valid email and password.");

			try {
				ud = dbh.getUser(email);
			} catch (CouchDbException e) {
				throw new IllegalArgumentException(
						"The email you entered does not belong to any account."
								+ "Please register and try to login again.");
			}

			if (!ud.isConfirmed())
				throw new SecurityException(
						"This account has not been activated yet.<br />"
								+ "If you have not received the confirmation email after 5 minutes "
								+ "try registering again.");

			if (!password.equals(ud.getPassword()))
				throw new IllegalArgumentException(
						"Incorrect Email/Password Combination.");

			token = SecureGen.generateSecureString(32);
			dbh.saveToken(ud.getEmail(), token);
			res.put("token", token);

		} catch(CouchDbException cde) {
			errorMessage = cde.getMessage();
		} catch (IllegalArgumentException iae) {
			errorMessage = iae.getMessage();
		} catch (SecurityException se) {
			errorMessage = se.getMessage();
		} catch (Exception e) {
			// This should never happen
			errorMessage = e.getMessage();
			errorType = e.getClass().getSimpleName();
			try {
				dbh.writeError(errorType, e);
			} catch (Exception e1) {
				// write the error in a log file
			}
		} finally {
			if (errorMessage != null) {
				success = false;
				res.put("error", errorMessage);
			}

			res.put("success", success);
		}

		return res;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
