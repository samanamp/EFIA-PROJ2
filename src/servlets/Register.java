package servlets;
import handlers.DBHandler;
import handlers.EmailHandler;
import handlers.SecureGen;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.conn.HttpHostConnectException;
import org.json.simple.JSONObject;

import data.UserData;
import data.Error;

/**
 * Servlet implementation class Register
 */
@WebServlet("/Register")
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	HttpServletRequest request;
	HttpServletResponse response;
	

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Register() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		try {
			this.request = request;
			this.response = response;
			JSONObject res = appLogic(request);

			// respond to the web browser
			response.setContentType("application/json");
			out.print(res);
			out.close();

		} catch (HttpHostConnectException e) {
			JSONObject res = new JSONObject();
			res.put("success", false);
			res.put("error",
					"Cannot connect to database, please try again later!");
			response.setContentType("application/json");
			out.print(res);
			out.close();
		} catch (Exception e) {
			JSONObject res = new JSONObject();
			DBHandler dbHandler = new DBHandler();
			dbHandler.addNewError(new Error("Register servlet",
					"General Error", e.getMessage()));
			res.put("success", false);
			res.put("error",
					"General error occured please contact administrator!");
			response.setContentType("application/json");
			out.print(res);
			out.close();
		}
	}

	private synchronized JSONObject appLogic(HttpServletRequest request)
			throws HttpHostConnectException, Exception {
		JSONObject res = new JSONObject();
		String email = request.getParameter("email");

		if (UserData.isValidEmail(email)) {

			DBHandler dbHandler = new DBHandler();
			UserData newUser = new UserData();

			if (dbHandler.ifUserExists(email)) {
				newUser = dbHandler.getUser(email);
				long remain = System.currentTimeMillis()
						- newUser.getConfirmationTimestamp();
				remain = remain / 1000; // Converting to seconds
				if(newUser.isConfirmed()){
					res.put("success", false);
					res.put("error",
							"You have confirmed this before, maybe you want to get a reminder email from \"Forgot my password\" link at home page.");
				}else if (remain > (5 * 60)) {

					sendConfirmationEmail(newUser);
					newUser.setConfirmationTimestamp(System.currentTimeMillis());
					dbHandler.updateUser(newUser);
					dbHandler.closeConnection();

					res.put("success", true);
				} else {
					res.put("success", false);
					res.put("error",
							"You have entered your email address less than 5 minutes before, If you havn't got your confirmation email, please try again in "
									+ (300-remain) + "seconds.");
				}
			} else {
				newUser.setEmail(email);
				/******** Send Confirmation Link ***************/
				String token = SecureGen.generateSecureString(32);
				newUser.setToken(token);

				sendConfirmationEmail(newUser);

				newUser.setConfirmationTimestamp(System.currentTimeMillis());
				/******* Save to DB **************************/

				dbHandler.addNewUser(newUser);
				dbHandler.closeConnection();

				res.put("success", true);
			}
		} else {
			res.put("success", false);
			res.put("error", "Wrong Email Address");
		}
		return res;
	}

	private void sendConfirmationEmail(UserData newUser) throws IOException {
		String confirmMessage = "Please confirm your registration by clicking on following link: \n"
				+ "<a href=\"http://"
				+ request.getLocalAddr()
				+ ":8080/proj1/Confirm?token="
				+ newUser.getToken()
				+ "&email="
				+ newUser.getEmail() + "\">Click me!</a>";
		EmailHandler emailHandler = new EmailHandler(newUser.getEmail(),
				"Account Confirmation", confirmMessage);
		emailHandler.start();
	}

}
