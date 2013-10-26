//TODO return if the user is admin of group or not.

var refreshMessages;
var lastMessage;
var groupID;

var chat = {
	init: function(){
			if(!login.verifySession())
				return;

			$(window).resize(function() {
				chat.setContentHeight();
			});
	
			chat.setContentHeight();
	
			$("#btn_sendMessage").button({text: false,icons: {primary: "ui-icon-comment"}});
			if(login.admin) $("#lnk_invite").button({text: false,icons: {primary: "ui-icon-person"}});
			if(!login.admin) $("#btn_leave").button({text: false,icons: {primary: "ui-icon-arrowstop-1-e"}});
			
			$("#btn_groups").button({icons: {primary: "btn_groups"}});
			$("#btn_chat").button({icons: {primary: "btn_chat"}});
			
			$("#btn_logout").button({icons: {primary: "btn_logout"}});		
			$("#btn_delete").button({icons: {primary: "btn_delete"}});
			$("#btn_reset").button({icons: {primary: "btn_reset"}});
	
			//register functions to  events
			$("#btn_sendMessage").bind("click", function() { chat.sendMessage(); });
			$("#message").bind("keypress", function(e) { if (e.which == 13) chat.sendMessage(); });
			$("#send").bind("focus", function() { $('#message').focus(); });
			
			chat.sendAJAX("", "");
			
			refreshMessages = setInterval(function(){chat.sendAJAX("", "");}, 1000);
			
			utils.setDialog("invite", "Invite a friend", function(){chat.sendInvitation();});
			
			if(!login.admin){
				$("#btn_leave").bind('click', function(){
					chat.sendLeave();
				} );
			}
			
	},
	stop: function(){
		clearInterval(refreshMessages);
	},
	getChatHTML: function(){
		return "<div id=\"chat_body\" class=\"ui-widget span-16 last\">"
		
					+ "<ul class=\"error\ push-2 span-12 last hide\"></ul>"
					
					+ "<div id=\"actions\" class=\"span-2\">"
						+ "<button id=\"btn_groups\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Groups</a></button>"
						+ "<button id=\"btn_reset\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Reset password</a></button>"
						+ "<button id=\"btn_delete\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Delete account</a></button>"
						+ "<button id=\"btn_logout\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Logout</a></button>"
					+ "</div>"
					
					+ "<div id=\"content_chat\" class=\"span-12\">"
						+ "<div id=\"group_name\" class=\"inner_content span-12 ui-button-text\"><h3>Chat name</h3></div>"
						+ "<div id=\"messages_history\" class=\"span-12\"></div>"
					+ "</div>"
				+ "</div>"
				
				+ "<div id=\"footer\" class=\"span-16 last\">"
					+ "<div id=\"toolbar\" class=\"ui-widget-header ui-corner-all push-3 span-9 last\">"
						+ "<div id=\"message_container\" class=\"prepend-1 span-5\">" 	
							+ "<input type=\"text\" id=\"message\" name=\"message\" class=\"span-5\" />"
						+ "</div>"
						
						+ "<div id=\"buttons_container\" class=\"span-3 last\">"
							+ "<button id=\"btn_sendMessage\">Send</button>"
							+ (login.admin ? "<button id=\"lnk_invite\">Invite</button>" : "")
							+ (!login.admin ? "<button id=\"btn_leave\">Leave group</button>" : "")
						+ "<\div>"								
					+ "</div>"
				+ "</div>"
			+ "</div>";
	},
	getInviteHTML: function(){
		return "<div id=\"content_invite\">"
					+ "<form id=\"frm_invite\" class=\"cmxform\">"
						+ "<div class=\"prepend-1 span-10 last\">"
						+ "<label for=\"txt_invite\" class=\"span-2 large\">Email:</label>"
						+ "<input type=\"text\" id=\"txt_invite\" name=\"txt_invite\" class=\"title span-7\" title=\"Please enter an email address\" size=\"30\" />"
						+ "</div>"
					
					+ "<ul class=\"error hide push-1 span-8 last\" id=\"error\"></ul>"
					+ "</form>"
				+ "</div>";
	},
	getGroupsHTML: function(){
		return "<div id=\"chat_body\" class=\"ui-widget span-16 last\">"
		
					+ "<ul class=\"error\ push-2 span-12 last hide\"></ul>"
					
					+ "<div id=\"actions\" class=\"span-2\">"
						+ "<button id=\"btn_groups\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Groups</a></button>"
						+ "<button id=\"btn_reset\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Reset password</a></button>"
						+ "<button id=\"btn_delete\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Delete account</a></button>"
						+ "<button id=\"btn_logout\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Logout</a></button>"
					+ "</div>"
					
					+ "<div id=\"content_chat\" class=\"span-13\">"
						+ "<div class=\"outer_container\">"
							+ "<div id=\"welcome\" class=\"ui-button-text\"><h3>Welcome " + login.email + "!</h3></div>"
							+ "<div id=\"groups_list\" class=\"inner_content\"></div>"
							
							+ "<div id=\"footer\" class=\"span-14 last\">"
								+ "<div id=\"toolbar\" class=\"ui-widget-header ui-corner-all push-2 span-9 last\">"
									+ "<div id=\"message_container\" class=\"prepend-1 span-5\">" 	
										+ "<input type=\"text\" id=\"message\" name=\"message\" class=\"span-5\" />"
									+ "</div>"
									
									+ "<div id=\"buttons_container\" class=\"span-3 last\">"
										+ "<button id=\"btn_sendMessage\">Send</button>"
										+ (login.admin ? "<button id=\"lnk_invite\">Invite</button>" : "")
										+ (!login.admin ? "<button id=\"btn_leave\">Leave group</button>" : "")
									+ "<\div>"								
								+ "</div>"
							+ "</div>"
						+ "</div>"
					+ "</div>"
				+ "</div>";
	},
	getHTML: function(){
		return chat.getGroupsHTML() + chat.getInviteHTML();
	},
	setContentHeight: function() {
			var bodyheight = $(window).height();
			$(".outer_container").height(bodyheight - 194 );
	},
	sendMessage: function(){//TODO testing
		login.verifySession();
		//Do not accept empty messages
		if ($('#message').val() == '') return;
		
		chat.sendAJAX(login.email, $('#message').val());//TODO
		$('#message').val('');
		
		//Scroll to bottom of the page
		$("#messages_history").animate({ scrollTop: $('#messages_history')[0].scrollHeight}, 500);
	},
	sendAJAX: function(username, message) {
		$.getJSON("Chat", {
			 "user" : username,
			 "message" : message,
			 "msghead" : lastMessage
			}, chat.addNewMessage);
	},
	addNewMessage: function(data) {//TODO return {messages:[{email:"",message:"",timestamp:""}]}
		if(data.success) {
			$.each(data.messages, function(i){
				var m = $(this)[0];
				var millis = new Date(m.timestamp).toString();
				var side_class = "bubble-" + (m.email == login.email ? "right" : "left");
				$('<div>'
					+ '<blockquote class="' + side_class + '">' + m.message + '</blockquote>'
					+ '<p>' + millis + ' - ' + m.email +  ' </p>'
					+ '</div>').appendTo('#messages_history');
				lastMessage = m.timestamp;
			});
	
		} else{
			$("#error").html(
					"<li>" + data.error + "</li>").show();
		}
	},
	sendInvitation: function(){//TODO testing
		login.verifySession();
		var settings = {
				form_id : "#frm_invitation",
				btn_id : "#btn_invite",
				url : "GroupServlet",
				data : {
					"email" : $("#txt_invitation").val(),
					"action": "invitation"
				},
				success : function(data) {
					if (data.success) {
						$("#frm_invitation")
								.append(
										"<ul class=\"success\"><li>"
												+ "Your invitation has been sent successfully."
												+ "</li></ul>");
					} else {
						$("#frm_invitation #error").html(
								"<li>" + data.error + "</li>").show();
					}
				},
				loadingText : "Sending invitation..."
			};
			utils.sendAjax(settings);
	},
	sendLeave: function(){ //TODO testing
		login.verifySession();
		var settings = {
			form_id : "",
			btn_id : "#btn_leave",
			url : "GroupServlet",
			data : {
				"email" : email,
				"groupID" : groupID
			},
			success : function(data) {
				if (data.success) {
					chat.getGroupsHTML();
				} else{
					$("#chat_body #error").html(
							"<li>" + data.error + "</li>").show();
				}
			}
		};
		utils.sendAjax(settings);
	}
};