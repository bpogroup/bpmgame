package nl.tue.bpmgame.bpmgamegui.viewmodels;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.io.Resource;

import nl.tue.bpmgame.dataaccess.model.User;

import nl.tue.bpmgame.bpmgamegui.services.ConstantProviderService;

public class Email {
	public final static String BODY_TEXT_TAG = "__BODYTEXT__";
	
	public final static String FROM_NAME = "BPM Game";	
	
	String bodyText;
	String recipientEmail;
	String recipientName;
	String fromName;
	String subject;
	String attachmentName;
	Resource attachment;

	Map<String,String> styleMap = new HashMap<String,String>();

	public Email(){
		fromName = FROM_NAME;
	}
		
	public void setDefaultStyle(){
		styleMap.put("__CUSTOMBGCOLORHEADER__","#4B5561");	
	
		styleMap.put("__CUSTOMCOLORBODY__","#000000");
	
		styleMap.put("__CUSTOMBGCOLORFOOTER1__","#F1F1F1");
		styleMap.put("__CUSTOMCOLORFOOTER1__","#444444");
		styleMap.put("__CUSTOMTEXTFOOTER1__","");

		styleMap.put("__CUSTOMBGCOLORFOOTER2__","#F1F1F1");
		styleMap.put("__CUSTOMCOLORFOOTER2__","#444444");
		styleMap.put("__CUSTOMTEXTFOOTER2__","http://www.bpmgame.org");
		
		styleMap.put("__LOGOIMG__", "<h4 class=\"logo\"><span>bpm</span>Game</h4>");
	}
		
	// ----------------------------------------------------------
	// TEMPLATES
	// ----------------------------------------------------------	 
		
	public static Email registrationEmail(User user, HttpServletRequest request){		
		String baseUrl = String.format("%s://%s:%d", request.getScheme(), request.getServerName(), request.getServerPort());
		String confirmUrl = baseUrl + ConstantProviderService.userConfirm + "/" + user.getUid();
		String bodyText = "<p>Dear " + user.getName() + ",</p>";
		bodyText += "<p>You have registered for the BPM Game. ";				
		bodyText += "We ask that you confirm your e-mail address. ";				
		bodyText += "You can do that by clicking <a href='" + confirmUrl + "'>here</a>. ";
		bodyText += "Should this link not work, you can also confirm your e-mail address by copying and pasting the following text in your browser.</p>";
		bodyText += "<p>"+confirmUrl+"</p>";
		bodyText += "<p>Best regards,</p>";
		bodyText += "<p></p>";
		bodyText += "<p>BPM Game</p>";

		Email email = new Email();
		email.setDefaultStyle();
		email.setBodyText(bodyText);
		email.setRecipientEmail(user.getEmail());
		email.setRecipientName(user.getName());
		email.setSubject("Request to confirm e-mail");
		
		return email;
	}

	public static Email passwordEmail(User user, String newPassword){
		String bodyText = "<p>Dear " + user.getName() + ",</p>";
		bodyText += "<p>Your password has been changed. When you log in, you can change it to a password of your choice ";								
		bodyText += "Your new password is:</p>";								
		bodyText += "<p>" + newPassword + "</p>";								
		bodyText += "<p>Best regards,</p>";
		bodyText += "<p></p>";
		bodyText += "<p>BPM Game</p>";

		Email email = new Email();
		email.setDefaultStyle();
		email.setBodyText(bodyText);
		email.setRecipientEmail(user.getEmail());
		email.setRecipientName(user.getName());
		email.setSubject("Your new password");
		
		return email;
	}
	
	// ----------------------------------------------------------
	// GETTERS AND SETTERS
	// ----------------------------------------------------------	 
	
	public void putStyle(String tag, String style){
		styleMap.put(tag, style);
	}
	
	public Map<String,String> getStyle(){
		return styleMap;
	}

	public String getBodyText() {
		return bodyText;
	}

	public void setBodyText(String bodyText) {
		this.bodyText = bodyText;
	}

	public String getRecipientEmail() {
		return recipientEmail;
	}

	public void setRecipientEmail(String recipientEmail) {
		this.recipientEmail = recipientEmail;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public Resource getAttachment() {
		return attachment;
	}

	public void setAttachment(Resource attachment) {
		this.attachment = attachment;
	}

	public String getRecipientName() {
		return recipientName;
	}

	public void setRecipientName(String recipientName) {
		this.recipientName = recipientName;
	}

	public String getFromName() {
		return fromName;
	}

	public void setFromName(String fromName) {
		this.fromName = fromName;
	}
}
