
package servlets;

import handlers.DBHandler;
import handlers.GroupHandler;
import handlers.UserSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import data.Group;
import data.Membership;
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
			} else {
				res = executeAddGroup(request, userSession, groupName);
			}
		} else if (method.equals("listgroups")) {
			res = executeListGroups(request, userSession);
		} else if (method.equals("adduser")) {
			String newUser = request.getParameter("newuser");
			String groupID = request.getParameter("group_id");
			if (newUser == null || newUser.equals("")) {
				res.put("success", false);
				res.put("error", "A valid new user name must be specified.");
			} else if (groupID == null || groupID.equals("")) {
				res.put("success", false);
				res.put("error", "A valid group_id must be specified.");
			} else {
				res = executeAddNewUser(request, userSession, newUser, groupID);
			}
		} else if (method.equals("confirm")) {
			/* Does not need userSession, it uses email and token for a completely
			 * different purpose */
			String groupID = request.getParameter("group_id");
			if (groupID == null || groupID.equals("")) {
				res.put("success", false);
				res.put("error", "A valid group_id must be specified.");
			} else {
				res = executeConfirmNewUser(request, response, groupID, email, token);
			}
		} else {
			res.put("success", false);
			res.put("error", "Could not recognize method: " + method);
		}
		
		// respond to the web browser
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		out.print(res);
		out.close();
	}

	/**
	 * 
	 * @param request
	 * @param userSession
	 * @param groupName
	 * @return
	 */
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
			res.put("success", true);
		} catch (CustomException ce) {
			res.put("success", false);
			res.put("error", "The group defined already exists for " + userSession.getEmail());
		} catch (Exception e) {
			res.put("success", false);
			res.put("error", "Unknown error at the Server: " + getStackTrace(e));
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param request
	 * @param userSession
	 * @return
	 */
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
				jgroup.put("owner", group.getOwner());
				jgroup.put("id", group.get_id());
				
				ArrayList<Membership> users = group.getUsers();
				JSONArray jusers = new JSONArray();
				for (Membership user : users) {
					jusers.add(user.getEmail());
				}
				jgroup.put("users", jusers);
				
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
	
	/**
	 * 
	 * @param request
	 * @param userSession
	 * @param groupName
	 * @return
	 */
	public synchronized JSONObject executeAddNewUser(HttpServletRequest request, 
			UserSession userSession, String newUser, String groupID) {
		
		JSONObject res = new JSONObject();
		
		try {
			/* Verify the session for that user */
			if (!userSession.isValid()) {
				res.put("success", false);
				res.put("error", userSession.getProblem());
				return res;
			}
			
			GroupHandler groupHandler = new GroupHandler(request.getLocalAddr());
			groupHandler.addNewUserToGroup(groupID, newUser, userSession.getEmail());
			res.put("success", true);
		} catch (CustomException ce) {
			res.put("success", false);
			res.put("error", ce.getMessage());
		} catch (Exception e) {
			res.put("success", false);
			res.put("error", "Unknown error at the Server: " + getStackTrace(e));
		}
		
		return res;
	}
	
	/**
	 * 
	 * @param request
	 * @param groupID
	 * @param email
	 * @param token
	 * @return
	 */
	public synchronized JSONObject executeConfirmNewUser(HttpServletRequest request, 
			HttpServletResponse response, String groupID, String email, String token) {
		
		JSONObject res = new JSONObject();
		String message = "";
		
		try {	
			GroupHandler groupHandler = new GroupHandler(request.getLocalAddr());
			groupHandler.confirmNewUser(groupID, email, token);
			message = "Congratulations! You have succesfully joined the group :)";
		} catch (CustomException ce) {
			message = "Uh oh! Something went wrong :( The problem was: " + ce.getMessage();
		} catch (Exception e) {
			message = "Internal Server Error: " + e.getMessage();
		}
		
		try {
			response.sendRedirect("confirm.html?message=" + message);
		} catch (IOException ioe) {
			res.put("success", false);
			res.put("error", "Unknown error at the Server: " + getStackTrace(ioe));
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
