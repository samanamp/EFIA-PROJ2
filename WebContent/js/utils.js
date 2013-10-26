var utils = {
		setDialog: function(action, title, sendFunction, rules){
			var form_id = "#frm_" + action;
			var txt_id = "#txt_" + action;
			var content_id = "#content_" + action;
			var button_id = "btn_" + action;
			var link_id = "#lnk_" + action;
			
			var frmValidator = $(form_id).validate({
				errorLabelContainer : $(form_id + " ul#error")
			});
			
			$(txt_id).rules('add', rules);

			$(content_id).dialog({
				title: title,
				buttons: [{
					id : button_id,
					text : title,
					'class': 'large',
					click : function() {
						utils.resetForm($(this));
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
					utils.resetForm($(this));
				}
			});

			$(link_id).bind("click", function() {
				$(content_id).dialog("open");
			});
		},
		resetForm: function (elem) {
			elem.find(".error").html("");
			$("#sending").remove();
			$(".success").remove();
		},
		showLoading: function (text) {
			if(text != "")
				return ("<div id=\"sending\" class=\"span-7 last\">" +
							"<h3>" +
								"<img src=\"css/images/loading.gif\" />" +
								text +
							"</h3>" +
						"</div>");
			else
				return ("<div id=\"sending\" class=\"notext\">" +
						"<img src=\"css/images/loading.gif\" />" +
						"</div>");
				
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
			
			if (settings.btn_id == undefined)
				settings.btn_id = "";

			$.ajax({
				type : "POST",
				dataType : "json",
				url : settings.url,
				data : settings.data,
				success : settings.success,
				beforeSend : function() {
					if(settings.btn_id != "")
						$(settings.btn_id).hide().after(utils.showLoading(settings.loadingText));
				},
				complete : function() {
					$(settings.btn_id).show();
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
		}
};