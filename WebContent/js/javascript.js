//var token = null;
//var email = null;
//var admin = null;
var token = "aa";
var admin = true;
var email = "thisistheemail@email.com";
var loggedOutContent;

var login = {
		init: function(){
			$.validator.setDefaults({
				submitHandler : function() {
					login.sendLogin();
					return false;
				},
				wrapper : "li",
				errorClass: "errorLabel"
			});

			$("#frmLogin").validate({
				rules : {
					txtEmail : {
						required : true,
						email : true
					},
					txtPassword : {
						required : true
					}
				},
				errorLabelContainer : $("#frmLogin ul#error")
			});

			$("#txtEmail,#txtPassword").bind("keypress", function(e) {
				$("#frmLogin #error").html("").hide();
				if (e.which == 13)
					return false;
			});
			
			$("#txt_register").bind("keypress", function(e) {
				$("#frm_register #error").html("").hide();
				if (e.which == 13)
					return false;
			});
			
			$("#txt_reminder").bind("keypress", function(e) {
				$("#frm_reminder #error").html("").hide();
				if (e.which == 13)
					return false;
			});

			$("#btnLogin").button({
				text : "Login"
			}).bind("click", function(event){
				if ($("#frmLogin").valid()) {
					login.sendLogin();
				}
				event.preventDefault();
			});

			loggedOutContent = $("#content_login");
			
			utils.setDialog("register", "Register", function(){login.sendRegister();});
			utils.setDialog("reminder", "Send reminder email", function(){login.sendReminder();});
		},
		getUserInfo: function(){
			if(email != null && admin != null && token != null)
				return {email: email, admin: admin};
			
			login.sendLogout();
		},
		sendLogin: function() {
			/*******************************************************************************
			 * Function to send a request to the Login Servlet it uses the sendAjax function
			 ******************************************************************************/
			var settings = {
				form_id : "#frmLogin",
				url : "Login",
				data : {
					"email" : $("#txtEmail").val(),
					"password" : $("#txtPassword").val()
				},
				success : function(data) {
					if (data.success) {
						token = data.token;
						email = $("#txtEmail").val();
						admin = data.admin;
						login.generateLoggedInContent();
					} else {
						$("#frmLogin #error").html(
								"<li>" + data.error + "</li>").show();
					}
				},
				loadingText : "Logging in..."
			};
			utils.sendAjax(settings);
		},
		generateLoggedInContent: function() {
			/*******************************************************************************
			 * Generates the content that will appear when the user is logged in
			 ******************************************************************************/
			$(".success").remove();
			var new_div = $("<div />", {
				id : "chat_container",
				'class': "push-4 span-16 ui-corner-all"
			});
			new_div.append(chat.getHTML)
				   .hide();

			$("#content_login").fadeOut("slow", function() {
				$("#content_login").remove();

				$(".container").append(new_div);

				$("#btn_logout").bind("click", function() {
					login.sendLogout();
				});

				$("#btn_reset").bind("click", function() {
					$(".success").remove();
					login.sendReset();
				});

				$("#btn_delete").bind("click", function() {
					$(".success").remove();
					login.sendDelete();
				});
				chat.init();
				new_div.fadeIn(300);

			});
		},
		sendRegister: function() {
			/*******************************************************************************
			 * Function to send a request to the Register Servlet it uses the sendAjax
			 * function
			 ******************************************************************************/
			var settings = {
				form_id : "#frm_register",
				url : "Register",
				data : {
					"email" : $("#txt_register").val()
				},
				success : function(data) {
					if (data.success) {
						$("#frm_register")
								.append(
										"<ul class=\"push-1 span-8 last success\"><li>"
												+ "An Email has been sent to your address. "
												+ "Please check your inbox and click "
												+ "in the confirmation link to activate your account."
												+ "</li></ul>");
					} else {
						$("#frm_register #error").html(
								"<li>" + data.error + "</li>").show();
					}
				},
				loadingText : "Registering user..."
			};
			utils.sendAjax(settings);
		},
		sendReminder: function() {
			/*******************************************************************************
			 * Function to send a request to the Remider Servlet it uses the sendAjax
			 * function
			 ******************************************************************************/
			var settings = {
				form_id : "#frm_reminder",
				url : "Reminder",
				data : {
					"email" : $("#txt_reminder").val()
				},
				success : function(data) {
					if (data.success) {
						$("#frm_reminder")
								.append(
										"<ul class=\"success\"><li>"
												+ "Your password has been sent to your address please "
												+ "check your inbox and try to login."
												+ "</li></ul>");
					} else {
						$("#frm_reminder #error").html(
								"<li>" + data.error + "</li>").show();
					}
				},
				loadingText : "Sending reminder..."
			};
			utils.sendAjax(settings);
		},
		sendLogout: function() {
			/*******************************************************************************
			 * Function to send a request to the Logout Servlet it uses the sendAjax
			 * function
			 ******************************************************************************/
			if (email != null && token != null) {
				var settings = {
					form_id : "#frmLogin",
					url : "Logout",
					data : {
						"email" : email,
						"token" : token
					},
					success : function(data) {
						token = null;
						email = null;
						admin = null;
						var error = null;
						if (!data.success) {
							error = data.error;
						}
						login.generateLoggedOutContent(error, "error");
					}
				};
				utils.sendAjax(settings);
			} else {
				login.generateLoggedOutContent("Invalid token.", "error");
			}
		},
		generateLoggedOutContent: function(message, messageClass) {
			/*******************************************************************************
			 * Generates the content that will appear when the user is logged out
			 ******************************************************************************/
			$(".success").remove();
			$("#chat_container").fadeOut(
					500,
					function() {
						$("#chat_container").remove();
						
						$(".container").append(loggedOutContent);
						$(".success").remove();
						if (message != null) {
							if (messageClass == "error") {
								$("#frmLogin #error").html(
										"<li>" + message + "</li>")
										.show();
							} else {
								$("#frmLogin").before(
										"<ul class=\"success\"><li>" + message
												+ "</li></ul>");
							}
						}
						chat.stop();
						login.init();
						loggedOutContent.fadeIn(300);
					});
		},
		sendReset: function() {
			/*******************************************************************************
			 * Function to send a request to the Reset Servlet it uses the sendAjax function
			 ******************************************************************************/
			if (email != null && token != null) {
				var settings = {
					form_id : "#frmLogin",
					url : "Reset",
					data : {
						"email" : email,
						"token" : token
					},
					success : function(data) {
						if (data.success) {
							$("#chat_container")
									.before(
											"<ul class=\"success\"><li>"
													+ "Your new password has been sent to your address please check your inbox and try to login."
													+ "</li></ul>");
						} else {
							login.generateLoggedOutContent(data.error, "error");
						}
					}
				};
				utils.sendAjax(settings);
			} else {
				login.generateLoggedOutContent("Invalid token.", "error");
			}
		},
		sendDelete: function () {
			/*******************************************************************************
			 * Function to send a request to the Delete Servlet it uses the sendAjax
			 * function
			 ******************************************************************************/
			if (email != null && token != null) {
				var settings = {
					form_id : "#frmLogin",
					url : "Delete",
					data : {
						"email" : email,
						"token" : token
					},
					success : function(data) {
						if (data.success) {
							login.generateLoggedOutContent(
									"We are sorry that you have decided to leave us. "
											+ "We hope you come back soon :)",
									"success");
						} else {
							login.generateLoggedOutContent(data.error, "error");
						}
					}
				};
				utils.sendAjax(settings);
			} else {
				login.generateLoggedOutContent("Invalid token.", "error");
			}
		}
};

$(document).ready(function() {
	login.init();
	login.generateLoggedInContent();
});