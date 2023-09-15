package nl.tue.bpmgame.bpmgamegui.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import nl.tue.bpmgame.bpmgamegui.viewmodels.Email;

public class EmailService {
	
	public static final String OWN_EMAIL = "info.bpmgame@gmail.com";
	
	@Autowired
	private JavaMailSenderImpl mailSender;

	private static final Logger logger = Logger.getLogger(EmailService.class);

	public void sendEmail(Email email){
	try {
        Resource resource = new ClassPathResource("nl/tue/bpmgame/bpmgamegui/resources/messagetemplate.html");
        BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()),1024);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            stringBuilder.append(line).append('\n');
        }
        br.close();
        
        String emailText = stringBuilder.toString();
        
        for (Map.Entry<String,String> style: email.getStyle().entrySet()){
    		emailText = emailText.replace((CharSequence)style.getKey(), (CharSequence)style.getValue());        	
        }
		emailText = emailText.replace((CharSequence)Email.BODY_TEXT_TAG, (CharSequence)email.getBodyText());
				
        final String finalEmailText = emailText;
        final String finalRecipientEmail = email.getRecipientEmail();
        final String finalRecipientName = email.getRecipientName();
        final String finalSubject = email.getSubject();
        final String finalAttachmentName = email.getAttachmentName();
        final Resource finalAttachment = email.getAttachment();

		mailSender.send(new MimeMessagePreparator() {
			public void prepare(MimeMessage mimeMessage) throws Exception {
				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				message.setFrom(new InternetAddress(OWN_EMAIL, email.getFromName()));
				message.setTo(new InternetAddress(finalRecipientEmail, finalRecipientName));
				message.setSubject(finalSubject);
				message.setText(finalEmailText, true);
				if (finalAttachmentName != null){
					message.addAttachment(finalAttachmentName, finalAttachment);
				}
			}
		});
	} catch (Exception e) {
        logger.error(e);
    }
}
}
