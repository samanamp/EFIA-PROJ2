//TODO return if the user is admin of group or not.

var refreshMessages;
var lastMessage = 0;
var groupID;

var chat = {
	init: function(){
			if(!login.verifySession())
				return;

			$(window).resize(function() {
				chat.setContentHeight();
			});
	
			chat.setContentHeight();
			chat.getGroups();
	},
	initGroupsManager: function(){
		
		utils.setDialog("create", "Create a new group", function(){chat.sendCreate();},{required : true, alphanumeric : true});
		$("#lnk_create").button({icons: {primary: "ui-icon-circle-plus"}});
		$("#groups_accordion").accordion({
		      collapsible: true,
		      icons: null,//TODO fix the icons
		      header: "div.group"
	    });
		
		$.each($(".btn_delete_group"), function(){
			$(this).button({icons: {primary: "ui-icon-circle-close"},text:false})
				.bind("click", function(){
					chat.sendDeleteGroup($(this).attr("id"));
				});
		});
		
		$.each($(".btn_delete_usergroup"), function(){
			$(this).button({icons: {primary: "ui-icon-closethick"},text:false})
				.bind("click", function(){
					var params = $(this).attr("id").split("_");
					chat.sendDeleteUserGroup(params[0],params[1]);
				});
		});
		

		$.each($(".btn_open_group"), function(){
			$(this).button({icons: {primary: "ui-icon-comment"},text:false})
				.bind("click", function(){
					var params = $(this).attr("id").split("_");
					login.admin = params[3];
					chat.sendOpenGroup(params[2]);
				});
		});
		
	},
	initChatRoom: function(group_id){
		chat.setContentHeight();
		//register functions to  events
		$("#btn_sendMessage")
			.bind("click", function() { chat.sendMessage(group_id); })
			.button({text: false,icons: {primary: "ui-icon-comment"}});
		$("#message").bind("keypress", function(e) { if (e.which == 13) chat.sendMessage(group_id); });
		$("#send").bind("focus", function() { $('#message').focus(); });
		
		if(login.admin) $("#lnk_invite").button({text: false,icons: {primary: "ui-icon-person"}});
		if(!login.admin) $("#btn_leave").button({text: false,icons: {primary: "ui-icon-arrowstop-1-e"}});
		
		chat.sendAJAX("", group_id);//TODO this will be called when opening a group chat
		
		refreshMessages = setInterval(function(){chat.sendAJAX("", group_id);}, 1000);
		
		alert($("#content_chat #content_invite").length);
		utils.setDialog("invite", "Invite a friend", function(){chat.sendInvitation(group_id);}, {required : true,email : true});
		
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
		return "<div class=\"outer_container\">"
					+ "<div id=\"group_name\" class=\"span-12 ui-button-text\"><h3>Chat name</h3></div>"
					+ "<div id=\"messages_history\" class=\"inner_content span-12\"></div>"
					
					+ "<div id=\"footer\" class=\"span-16 last\">"
						+ "<div id=\"toolbar\" class=\"ui-widget-header ui-corner-all push-1 span-9 last\">"
							+ "<div id=\"message_container\" class=\"prepend-1 span-5\">" 	
							+ "<input type=\"text\" id=\"message\" name=\"message\" class=\"span-5\" />"
						+ "</div>"
					
						+ "<div id=\"buttons_container\" class=\"span-3 last\">"
							+ "<button id=\"btn_sendMessage\">Send</button>"
							+ (login.admin ? "<button id=\"lnk_invite\">Invite</button>" : "")
							+ (!login.admin ? "<button id=\"btn_leave\">Leave group</button>" : "")
						+ "<\div>"								
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
	getGroupsHTML: function(groups_list){
		var html = "<div id=\"chat_body\" class=\"ui-widget span-16 last\">"
		
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
							+ "<div id=\"groups_list\" class=\"inner_content\">" + groups_list + "</div>"
							
							+ "<div id=\"footer\" class=\"span-14 last\">"
								+ "<div id=\"toolbar\" class=\"ui-widget-header ui-corner-all push-2 span-9 last\">"
									+ "<div id=\"buttons_container\" class=\"prepend-2 span-5 last\">"
										+ "<button id=\"lnk_create\">Create new group</button>"
									+ "<\div>"								
								+ "</div>"
							+ "</div>"
						+ "</div>"
					+ "</div>"
				+ "</div>";
		
		html += chat.getCreateHTML();
		return html;
	},
	getCreateHTML: function(){
		return "<div id=\"content_create\">"
					+ "<form id=\"frm_create\" class=\"cmxform\">"
						+ "<div class=\"prepend-1 span-10 last\">"
						+ "<label for=\"txt_create\" class=\"span-4 large\">Group Name:</label>"
						+ "<input type=\"text\" id=\"txt_create\" name=\"txt_create\" class=\"title span-5\" " 
						+ "title=\"Please enter an alphanumeric name for your group.\" size=\"30\" />"
						+ "</div>"
					
					+ "<ul class=\"error hide push-1 span-8 last\" id=\"error\"></ul>"
					+ "</form>"
				+ "</div>";
	},
	setContentHeight: function() {
			var bodyheight = $(window).height();
			$(".outer_container").height(bodyheight - 194 );
	},
	sendMessage: function(group_id){//TODO testing
		login.verifySession();
		//Do not accept empty messages
		if ($('#message').val() == '') return;
		
		chat.sendAJAX($('#message').val(), group_id);//TODO
		$('#message').val('');
		
		//Scroll to bottom of the page
		$("#messages_history").animate({ scrollTop: $('#messages_history')[0].scrollHeight}, 500);
	},
	sendAJAX: function(message, group_id) {
		$.getJSON("Chat", {
			 "user" : login.email,
			 "token" : login.token,
			 "group_id" : group_id,
			 "message" : message,
			 "msghead" : lastMessage
			}, chat.addNewMessage);
	},
	addNewMessage: function(data) {//TODO return {messages:[{email:"",message:"",timestamp:""}]}
		if(data.success) {
			$.each(data.messages, function(i){
				var m = $(this)[0];
				var millis = new Date(m.timestamp).toString();
				var side_class = "bubble-" + (m.user == login.email ? "right" : "left");
				$('<div>'
					+ '<blockquote class="' + side_class + '">' + m.message + '</blockquote>'
					+ '<p>' + millis + ' - ' + m.user +  ' </p>'
					+ '</div>').appendTo('#messages_history');
				lastMessage = m.timestamp;
			});
	
		} else{
			$("#error").html(
					"<li>" + data.error + "</li>").show();
		}
	},
	sendInvitation: function(group_id){//TODO testing
		login.verifySession();
		var settings = {
				form_id : "#frm_invitation",
				btn_id : "#btn_invite",
				url : "GroupServlet",
				data : {
					"email" : login.email,
					"token" : login.token,
					"method": "adduser",
					"group_id": group_id,
					"newuser" : $("#txt_invite").val()
				},
				success : function(data) {
					if (data.success) {
						$("#frm_invite")
								.append(
										"<ul class=\"success push-1 span-8 last\"><li>"
												+ "Your invitation has been sent successfully."
												+ "</li></ul>");
					} else {
						$("#frm_invite #error").html(
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
				"email" : login.email,
				"groupID" : groupID
			},
			success : function(data) {
				if (data.success) {
					chat.init();
				} else{
					$("#chat_body #error").html(
							"<li>" + data.error + "</li>").show();
				}
			}
		};
		utils.sendAjax(settings);
	},
	sendCreate: function(){//TODO testing
		login.verifySession();
		var settings = {
				form_id : "#frm_create",
				btn_id : "#btn_create",
				url : "GroupServlet",
				data : {
					"method": "addgroup",					
					"groupname" : $("#txt_create").val(),
					"email" : login.email,
					"token" : login.token
				},
				success : function(data) {
					if (data.success) {
						$("#frm_create")
								.append(
										"<ul class=\"success push-1 span-8 last\"><li>"
												+ "Your group has been created successfully."
												+ "</li></ul>");
					} else {
						$("#frm_create #error").html(
								"<li>" + data.error + "</li>").show();
					}
				},
				loadingText : "Creating group..."
			};
			utils.sendAjax(settings);
	},
	sendDeleteGroup: function(id){
		login.verifySession();
		alert("should delete the group " + id);
//		var settings = {
//			form_id : "",
//			btn_id : button,
//			url : "GroupServlet",
//			data : {
//				"email" : login.email,
//				"groupID" : groupID
//			},
//			success : function(data) {
//				if (data.success) {
//					chat.init();
//				} else{
//					$("#chat_body #error").html(
//							"<li>" + data.error + "</li>").show();
//				}
//			}
//		};
//		utils.sendAjax(settings);
	},
	sendDeleteUserGroup: function(group, user){
		login.verifySession();
		alert("should delete the user " + user + " from group " + group);
//		var settings = {
//			form_id : "",
//			btn_id : button,
//			url : "GroupServlet",
//			data : {
//				"email" : login.email,
//				"groupID" : groupID
//			},
//			success : function(data) {
//				if (data.success) {
//					chat.init();
//				} else{
//					$("#chat_body #error").html(
//							"<li>" + data.error + "</li>").show();
//				}
//			}
//		};
//		utils.sendAjax(settings);
	},
	sendOpenGroup: function(group){
		login.verifySession();
		$(".outer_container").fadeOut(300, function(){
			$(".outer_container").remove();
			
			$("#content_chat").html(chat.getChatHTML()+chat.getInviteHTML());
			chat.initChatRoom(group);
		});
	},
	getGroups: function(){
		login.verifySession();
		var settings = {
			form_id : "",
//				btn_id : "#btn_leave",
			url : "GroupServlet",
			data : {
				"email" : login.email,
				"token" : login.token,
				"method" : "listgroups"
			},
			success : function(data) {
				var groups_list;
				if (data.success) {
					groups_list = "<div id=\"groups_accordion\" class=\"span-12 last\">"
					+ (chat.getGroupsList(data.groups))
					+ "</div>";
					
					var chat_container = $("<div />", {
						id : "chat_container",
						'class': "push-4 span-16 ui-corner-all"
					});
					
					var groups_html = chat.getGroupsHTML(groups_list);
					
					chat_container.append(groups_html);
					
					$("#content_login").fadeOut("slow", function() {
						$("#content_login").remove();

						$(".container").append(chat_container);
						$("#btn_logout")
							.bind("click", function() {login.sendLogout();})
							.button({icons: {primary: "btn_logout"}});
						$("#btn_reset")
							.bind("click", function() {$(".success").remove(); login.sendReset();})
							.button({icons: {primary: "btn_reset"}});
						$("#btn_delete")
							.bind("click", function() {$(".success").remove();login.sendDelete();})
							.button({icons: {primary: "btn_delete"}});
						
						$("#btn_groups").button({icons: {primary: "btn_groups"}});
						$("#btn_chat").button({icons: {primary: "btn_chat"}});
						
						chat.initGroupsManager();
						
						chat_container.fadeIn(300);

					});
				} else{
					$("#chat_body #error").html(
							"<li>" + data.error + "</li>").show();
				}
			}
		};
		utils.sendAjax(settings);
	},
	getGroupsList: function(groups){
		var list = "";
		$.each(groups, function(i, g){
			var admin = (g.owner == login.email);
			var side_class = "group-" + (admin ? "right" : "");
			list += '<div class="group span-12">'
					+ '<h3 class="' + side_class + ' span-10">' + g.name + '</h3>'
					+ '<button id=\"btn_open_' + g.id + '_' + admin + '\" class="btn_open_group"><a href=\"javascript:void(0)\">Open group</a></button>'
					+ '<button id=\"' + g.id + '\" class="btn_delete_group"><a href=\"javascript:void(0)\">Delete</a></button>'
				 + '</div>'
					+ '<div>'
						+ chat.getUsersList(g)
					+'</div>';
		});
		
		return list;
	},
	getUsersList: function(g){
		var list = "";
		$.each(g.users, function(i,u){
			list = '<div class="span-10">'
					+ '<button id=\"' + g.id + '_' + u + '\" class="btn_delete_usergroup"><a href=\"javascript:void(0)\">Remove user</a></button>'
					+ u 
					+ '</div>';
		 });
		return list;
	}
};