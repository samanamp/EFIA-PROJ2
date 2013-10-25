//TODO return if the user is admin of group or not.
//
var divcounter = 0;
var refreshMessages;
var chat = {
	init: function(){
			$(window).resize(function() {
				chat.setContentHeight();
			});
	
			chat.setContentHeight();
	
			$("#btn_sendMessage").button({text: false,icons: {primary: "ui-icon-comment"}});
			$("#btn_invite").button({text: false,icons: {primary: "ui-icon-person"}});
			$("#btn_leave").button({text: false,icons: {primary: "ui-icon-arrowstop-1-e"}});
			
			$("#btn_home").button({icons: {primary: "btn_home"}});
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
	},
	stop: function(){
		clearInterval(refreshMessages);
	},
	getBodyHTML: function(){
		return "<div id=\"chat_body\" class=\"ui-widget span-16 last\">"
		
					+ "<ul class=\"error\ push-2 span-12 last hide\"></ul>"
					
					+ "<div id=\"actions\" class=\"span-2\">"
						//+ "<button id=\"btn_home\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Home</a></button>"
						+ "<button id=\"btn_home\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Home</a></button>"
						+ "<button id=\"btn_chat\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Chat</a></button>"
					+ "</div>"
					
					+ "<div id=\"messages_history\" class=\"span-12\"></div>"
					
					+ "<div id=\"settings\" class=\"span-2 last\">"
						+ "<button id=\"btn_logout\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Logout</a></button>"
						+ "<button id=\"btn_delete\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Delete account</a></button>"
						+ "<button id=\"btn_reset\" class=\"span-2 last\"><a href=\"javascript:void(0)\">Reset password</a></button>"
					+ "</div>"
				+ "</div>"
				
				+ "<div id=\"footer\" class=\"span-16 last\">"
					+ "<div id=\"toolbar\" class=\"ui-widget-header ui-corner-all push-3 span-9 last\">"
						+ "<div id=\"message_container\" class=\"prepend-1 span-5\">" 	
							+ "<input type=\"text\" id=\"message\" name=\"message\" class=\"span-5\" />"
						+ "</div>"
						
						+ "<div id=\"buttons_container\" class=\"span-3 last\">"
							+ "<button id=\"btn_sendMessage\">Send</button>"
							+ "<button id=\"btn_invite\">Invite</button>"
							+ "<button id=\"btn_leave\">Leave group</button>"
						+ "<\div>"								
					+ "</div>"
				+ "</div>"
			+ "</div>";
	},
	getHTML: function(){
		return chat.getBodyHTML()
	},
	setContentHeight: function() {
			var bodyheight = $(window).height();
			$("#chat_body").height(bodyheight - 162 );
	},
	sendMessage: function(){
		//Do not accept empty messages
		if ($('#message').val() == '') return;
		
		chat.sendAJAX($('#username').val(), $('#message').val());
		$('#message').val('');
		
		//Scroll to bottom of the page
		$("#messages_history").animate({ scrollTop: $('#messages_history')[0].scrollHeight}, 500);
	},
	sendAJAX: function(username, message) {
		$.getJSON("Chat", {
			 "user" : username,
			 "message" : message,
			 "msghead" : divcounter
			}, chat.addNewMessage);
	},
	addNewMessage: function(data) {
		if(data.success) {
			$.each(data.messages, function(i){
				var millis = new Date($(this)[0].timestamp);
				$('<div id="message' + divcounter + '">'
					+ '<blockquote class="bubble-right">' + $(this)[0].message + '</blockquote>'
					+ '<p>' + millis.toString() + ' - ' + $(this)[0].user +  ' </p>'
					+ '</div>').appendTo('#messages_history');
				divcounter++;
			});
	
		} else{
			alert(data.error);
		}
	}
}