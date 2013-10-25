var token = null;
var email = null;
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
			
			login.setDialog("register", "Register", function(){login.sendRegister();});
			login.setDialog("reminder", "Send reminder email", function(){login.sendReminder();});
		},
		setDialog: function(action, title, sendFunction){
			var form_id = "#frm_" + action;
			var txt_id = "#txt_" + action;
			var content_id = "#content_" + action;
			var button_id = "#btn_" + action;
			var link_id = "#lnk_" + action;
			
			var frmValidator = $(form_id).validate({
				errorLabelContainer : $(form_id + " ul#error")
			});
			
			$(txt_id).rules('add', {
				required : true,
				email : true
			});

			$(content_id).dialog({
				title: title,
				buttons: [{
					id : button_id,
					text : title,
					'class': 'large',
					click : function() {
						login.resetForm($(this));
						if ($(form_id).valid()) {
							sendFunction();
						}
					}
				}],
				autoOpen : false,
				show : {
					effect : "blind",
					duration : 500
				},
				hide : {
					effect : "explode",
					duration : 500
				},
				resizable : false,
				width : 450,
				modal : true,
				close: function() {
					frmValidator.resetForm();
					$(this).find("input").removeClass("error").val("");
					login.resetForm($(this));
				}
			});

			$(link_id + " a").bind("click", function() {
				$(content_id).dialog("open");
			});
		},
		resetForm: function (elem) {
			elem.find(".error").html("");
			$("#sending").remove();
			$(".success").remove();
		},
		showLoading: function (text) {
			return "<div id=\"sending\" class=\"span-7 last\">" +
						"<h3>" +
							"<img src=\"css/images/loading.gif\" />" +
							text +
						"</h3>" +
					"</div>";
		},
		sendAjax: function(settings) {
			/*******************************************************************************
			 * Generic function to send an Ajax request to the Servlets. receives a JS
			 * object (settings) containing: form_id, url, data, success
			 ******************************************************************************/
			if (settings.form_id == undefined)
				settings.form_id = "";

			if (settings.url == undefined)
				settings.url = "";

			if (settings.data == undefined)
				settings.data = {};

			if (settings.success == undefined)
				settings.success = function() {
				};

			if (settings.loadingText == undefined)
				settings.loadingText = "";

			$.ajax({
				type : "POST",
				dataType : "json",
				url : settings.url,
				data : settings.data,
				success : settings.success,
				beforeSend : function() {
					if ($(settings.form_id).parent(".ui-dialog-content").length > 0) {
						$(settings.form_id).parent(".ui-dialog-content")
								.siblings(".ui-dialog-buttonpane").find(
										".ui-dialog-buttonset").find("button")
								.hide();

						$(settings.form_id).parent(".ui-dialog-content")
								.siblings(".ui-dialog-buttonpane").find(
										".ui-dialog-buttonset").append(
												login.showLoading(settings.loadingText));

					} else {
						$(settings.form_id).find("input[type=submit]").hide()
								.after(login.showLoading(settings.loadingText))// login
					}

				},
				complete : function() {
					$(settings.form_id).parent(".ui-dialog-content").siblings(
							".ui-dialog-buttonpane").find("button").show();

					$(settings.form_id).find("input[type=submit]").show();// for
																			// login
																			// case
					$("#sending").remove();
					$(settings.form_id).find(settings.form_id + " .success")
							.remove();
				},
				error : function(jqXHR, textStatus, errorThrown) {
					// this should never happen
					$(settings.form_id)
							.find("#error")
							.html(
									"<li>An error has ocurred: "
											+ textStatus
											+ ((errorThrown != undefined && errorThrown != "") ? ": "
													+ errorThrown
													: "")
											+ ". <br />Please contact the system administrator.</li>")
							.show();
				}
			});
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
						login.generateLoggedInContent();
					} else {
						$("#frmLogin #error").html(
								"<li>" + data.error + "</li>").show();
					}
				},
				loadingText : "Logging in..."
			};
			login.sendAjax(settings);
		},
		generateLoggedInContent: function() {
			/*******************************************************************************
			 * Generates the content that will appear when the user is logged in
			 ******************************************************************************/
			$(".success").remove();
			var new_div = $("<div />", {
				id : "content_chat",
				'class': "push-4 span-16"
			});
			new_div.append(chat.getHTML)
				   .hide();

			$("#content_login").fadeOut("slow", function() {
				$("#content_login").remove();

				$(".container").append(new_div);

				$("#lnkLogout").bind("click", function() {
					login.sendLogout();
				});

				$("#lnkReset").bind("click", function() {
					$(".success").remove();
					login.sendReset();
				});

				$("#lnkDelete").bind("click", function() {
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
			login.sendAjax(settings);
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
			login.sendAjax(settings);
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
						var error = null;
						if (!data.success) {
							error = data.error;
						}
						login.generateLoggedOutContent(error, "error");
					}
				};
				login.sendAjax(settings);
			} else {
				login.generateLoggedOutContent("Invalid token.", "error");
			}
		},
		generateLoggedOutContent: function(message, messageClass) {
			/*******************************************************************************
			 * Generates the content that will appear when the user is logged out
			 ******************************************************************************/
			$(".success").remove();
			$("#content_chat").fadeOut(
					500,
					function() {
						$("#content_chat").remove();
						
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

						login.init();
						loggedOutContent.fadeIn(300);
						chat.stop();
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
				login.sendAjax(settings);
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
				login.sendAjax(settings);
			} else {
				login.generateLoggedOutContent("Invalid token.", "error");
			}
		}
};

$(document).ready(function() {
	login.init();
	login.generateLoggedInContent();
});