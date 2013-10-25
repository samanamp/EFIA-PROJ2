package servlets;
import handlers.DBHandler;
import handlers.SendPassword;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.conn.HttpHostConnectException;
import org.json.simple.JSONObject;

import data.Error;
import data.UserData;

/**
 * Servlet implementation class Reminder
 */
@WebServlet("/Reminder")
public class Reminder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final long timeBetweenTries = 300000;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Reminder() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		try{		
		JSONObject res = appLogic(request);
		response.setContentType("application/json");
		out.print(res);
		out.close();
		}catch(HttpHostConnectException e){
			JSONObject res = new JSONObject();
			res.put("success", false);
			res.put("error", "Cannot connect to database, please try again later!");
			response.setContentType("application/json");
			out.print(res);
			out.close();
		}catch(Exception e){
			JSONObject res = new JSONObject();
			DBHandler dbHandler = new DBHandler();
			dbHandler.addNewError(new Error("Register servlet", "General Error", e.getMessage()));
			res.put("success", false);
			res.put("error", "General error occured please contact administrator!");
			response.setContentType("application/json");
			out.print(res);
			out.close();
		}

	}

	private synchronized JSONObject appLogic(HttpServletRequest request) throws IOException, HttpHostConnectException, Exception {
		// if email address is invalid?
		JSONObject res = new JSONObject();
		try {
			String email = request.getParameter("email");
			if (UserData.isValidEmail(email)) {
				DBHandler dbHandler = new DBHandler();
				UserData userData;
				if (!dbHandler.ifUserExists(email))
					throw new Exception(
							"Email Doesn't exist, you may probably want to Register");
				
				userData = dbHandler.getUser(email);
				if (!userData.isConfirmed())
					throw new Exception("The email should be confirmed at first");
				
				SendPassword.sendReminder(userData, timeBetweenTries);
				
				userData.setReminderTimestamp(System.currentTimeMillis());
				
				dbHandler.updateUser(userData);
				res.put("success", true);
			} else{
				res.put("success", false);
				res.put("error", "Wrong Email Address");
			}
		} catch (Exception e) {
			res.put("success", false);
			res.put("error", e.getMessage());
		}
		return res;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

}
