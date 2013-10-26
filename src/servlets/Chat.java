
package servlets;

import handlers.DBHandler;
import handlers.GroupHandler;
import handlers.UserSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.lightcouch.NoDocumentException;

import data.Group;
import data.Membership;
import data.Message;
import exceptions.CustomException;

/**
 * Servlet implementation class Chat
 */
@WebServlet("/Chat")
public class Chat extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DBHandler dbh;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Chat() {
        super();
        dbh = new DBHandler();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// create a JSON object (using json-simple)
		JSONObject res = executeSynchronizedLogic(request);
		
		// respond to the web browser
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(res);
		out.close();
	}
	
	public synchronized JSONObject executeSynchronizedLogic(HttpServletRequest request) {
		
		JSONObject res = new JSONObject();
		
		try {
			// get the request parameters
			String user = request.getParameter("user");
			String token = request.getParameter("token");
			String message = request.getParameter("message");
			String groupID = request.getParameter("group_id");
			long msgHead = Long.parseLong(request.getParameter("msghead"));
			
			//Verify session
			UserSession userSession = new UserSession(user);
			userSession.verifySession(dbh, user, token);
			if (!userSession.isValid()) {
				res.put("success", false);
				res.put("error", userSession.getProblem());
				return res;
			}
			
			//Verify that the group exists
			GroupHandler groupHandler = new GroupHandler(request.getLocalAddr());
			try {
				if (groupID == null || groupID.equals("")) {
					res.put("success", false);
					res.put("error", "A group id must be defined.");
					return res;
				}
				Group group = groupHandler.getGroup(groupID);
				/* Check if the user is a member */
				if (!groupHandler.userIsMember(user, group)) {
					res.put("success", false);
					res.put("error", "The user " + user + " does not belong to this group");
					return res;
				}
			} catch (NoDocumentException nde) {
				res.put("success", false);
				res.put("error", "The group selected does not exist.");
				return res;
			}
	
			//Write message to the DB
			if (message != null) {
				if (!message.equals("")) {
					long millis = Calendar.getInstance().getTimeInMillis();
					
					try {
						//insert in DB		
						groupHandler.addNewMessageToGroup(groupID, new Message(user, message, millis));
					} catch (CustomException ce) {
						res.put("success", false);
						res.put("error", ce.getMessage());
						return res;
					} catch(Exception e) {
						res.put("success", false);
						res.put("error", "Internal error: " + getStackTrace(e));
						return res;
					}
				}
			}
			
			//Obtain messages 
			ArrayList<Message> messages = groupHandler.getMessagesOfGroup(groupID,user);
			JSONArray jmessages = new JSONArray();
			for (Message msg : messages) {
				if (msg.getTimestamp() <= msgHead)
					continue;
				JSONObject jmessage = new JSONObject();
				jmessage.put("user", msg.getUser());
				jmessage.put("message", msg.getMessage());
				jmessage.put("timestamp", msg.getTimestamp());
				jmessages.add(jmessage);
			}			
			res.put("success", true);
			res.put("messages", jmessages);
		} catch (NumberFormatException nfe) {
			res.put("success", false);
			res.put("error", "Wrong format for the message head.");
		} catch (Exception e) {
			res.put("success", false);
			res.put("error", "Unknown error at the Server: " + getStackTrace(e));
		}
		
		return res;
	}
	
	public String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		return sw.toString(); 
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
