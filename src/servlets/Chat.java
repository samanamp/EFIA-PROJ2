
package servlets;

import handlers.DBHandler;

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

import org.json.simple.JSONObject;

import data.Message;

/**
 * Servlet implementation class Chat
 */
@WebServlet("/Chat")
public class Chat extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ArrayList<Message> msgList = new ArrayList<Message>();
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
			String message = request.getParameter("message");
			int msgHead = Integer.parseInt(request.getParameter("msghead"));
	
			//Write message to the DB and append it to the list
			if (message != null) {
				if (!message.equals("")) {
					long millis = Calendar.getInstance().getTimeInMillis();
					
					String _id = Integer.toString(msgList.size());
					try {
						//insert in DB
						//dbh.putMessage(new Message(user, message, millis));
						//add message to the list
						msgList.add(new Message(user, message, millis));
					} catch(Exception e) {
						//in case of an error inserting, it will enter here
						res.put("success", false);
						res.put("error", "Internal error: " + getStackTrace(e));
					}
				}
			}
			
			//Obtain messages from the list if needed
			if (msgHead < msgList.size()) {
				JSONObject listJson = new JSONObject();
				
				for (int i = msgHead; i < msgList.size(); i++) {
					JSONObject msgjson = new JSONObject();
					
					Message msg = msgList.get(i);
					msgjson.put("user", msg.getUser());
					msgjson.put("message", msg.getMessage());
					msgjson.put("timestamp", msg.getTimestamp());
					
					listJson.put(i, msgjson);	
				}
				res.put("messages", listJson);
			} else {
				res.put("messages", "");
			}
			res.put("success", true);
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
