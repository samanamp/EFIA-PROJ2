
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

import data.Group;
import data.Message;
import data.UserData;
import exceptions.CustomException;

/**
 * Servlet implementation class GroupServlet
 */
@WebServlet("/GroupServlet")
public class GroupServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private DBHandler dbh;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GroupServlet() {
        super();
        dbh = new DBHandler();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		
		JSONObject res = new JSONObject();
		
		// create a JSON object (using json-simple)
		String method = request.getParameter("method");
		String email = request.getParameter("email");
		
		//Verify session
		String token = (String) request.getParameter("token");
		UserSession userSession = new UserSession(email);
		userSession.verifySession(dbh, email, token);
		
		if (method == null || method.equals("")) {
			res.put("success", false);
			res.put("error", "A method has to be defined.");
		} else if (method.equals("addgroup")) {
			String groupName = request.getParameter("groupname");
			if (groupName == null || groupName.equals("")) {
				res.put("success", false);
				res.put("error", "A group name must be defined.");
			}
			res = executeAddGroup(request, userSession, groupName);
		} else if (method.equals("listgroups")) {
			res = executeListGroups(request, userSession);
		} else {
			res.put("success", false);
			res.put("error", "Could not recognize method: ." + method);
		}
		
		// respond to the web browser
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(res);
		out.close();
	}

	public synchronized JSONObject executeAddGroup(HttpServletRequest request, 
			UserSession userSession, String groupName) {
		
		JSONObject res = new JSONObject();
		
		try {
			/* Verify the session for that user */
			if (!userSession.isValid()) {
				res.put("success", false);
				res.put("error", userSession.getProblem());
				return res;
			}
			
			GroupHandler groupHandler = new GroupHandler(request.getLocalAddr());
			groupHandler.addNewGroup(groupName, userSession.getEmail());
			
		} catch (CustomException ce) {
			res.put("success", false);
			res.put("error", "The group defined already exists for " + userSession.getEmail());
		} catch (Exception e) {
			res.put("success", false);
			res.put("error", "Unknown error at the Server: " + getStackTrace(e));
		}
		
		return res;
	}
	
	public synchronized JSONObject executeListGroups(HttpServletRequest request, 
			UserSession userSession) {
		
		JSONObject res = new JSONObject();
		
		try {
			/* Verify the session for that user */
			if (!userSession.isValid()) {
				res.put("success", false);
				res.put("error", userSession.getProblem());
				return res;
			}
			
			GroupHandler groupHandler = new GroupHandler(request.getLocalAddr());
			ArrayList<Group> groups = groupHandler.getGroups(userSession.getEmail());
			
			JSONArray jgroups = new JSONArray();
			for (Group group : groups) {
				JSONObject jgroup = new JSONObject();
				jgroup.put("name", group.getName());
				jgroup.put("id", group.get_id());
				jgroups.add(jgroup);
			}			
			res.put("success", true);
			res.put("groups", jgroups);
			
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
