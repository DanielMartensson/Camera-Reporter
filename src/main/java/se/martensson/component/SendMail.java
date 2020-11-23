package se.martensson.component;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class SendMail {
	
	@Autowired
    public JavaMailSender emailSender;
	
	@Value("${mail.subject}")
	private String subject;
	
	
	public Boolean sendMailTo(String destination, String message) {
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage(); 
		simpleMailMessage.setTo(destination);
		simpleMailMessage.setSubject(subject);
		simpleMailMessage.setText(message);
        emailSender.send(simpleMailMessage);
        Boolean messageHasBeenSent = true;
        return messageHasBeenSent;
		
	}
	
}