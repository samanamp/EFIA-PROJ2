var divcounter = 0;
var chat = {
	init: function(){
			$(window).resize(function() {
				chat.setContentHeight();
			});
	
			chat.setContentHeight();
	
			$("#btn_sendMessage").button({
				text: false,
				icons: {
					primary: "ui-icon-comment"
				}
			});
			
			$("#btn_invite").button({
				text: false,
				icons: {
					primary: "ui-icon-person"
				}
			});
	
			//register functions to  events
			$("#btn_sendMessage").bind("click", function() { chat.sendMessage(); });
			$("#message").bind("keypress", function(e) { if (e.which == 13) chat.sendMessage(); });
			$("#send").bind("focus", function() { $('#message').focus(); });
			
			chat.sendAJAX("", "");
			
			setInterval(function(){chat.sendAJAX("", "");}, 1000);
	},
	getSettingsHTML: function(){
		return "<div id=\"settings\" class=\"span-16 last\">"
					+ "<h4 id=\"lnkLogout\" class=\"span-2 right last\"><a href=\"javascript:void(0)\">Logout</a></h4>"
					+ "<h4 id=\"lnkDelete\" class=\"span-3 right\"><a href=\"javascript:void(0)\">Delete account</a></h4>"
					+ "<h4 id=\"lnkReset\" class=\"span-4 right\"><a href=\"javascript:void(0)\">Reset my password</a></h4>"
				+ "</div>";
	},
	getBodyHTML: function(){
		return "<div id=\"chat_body\" class=\"ui-widget span-16 last\">"
				+ "<ul class=\"error\ push-2 span-12 last hide\"></ul>"
				+ "<div id=\"outercontainer\" class=\"span-16 last\">"
					+ "<div id=\"innercontainer\" class=\"span-16 last\"></div>"
						+ "<div id=\"messages_history\" class=\"push-2 span-12 last\"></div>"
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
						+ "<\div>"
							
					+ "</div>"
				+ "</div>"
			+ "</div>";
	},
	getHTML: function(){
		return chat.getSettingsHTML() + chat.getBodyHTML()
	},
	setContentHeight: function() {
			var bodyheight = $(window).height();
			$("#messages_history").height(bodyheight - 200 );
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