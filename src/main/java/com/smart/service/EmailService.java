package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

public boolean sendEmail(String subject, String message, String to) {
		
		boolean f = false;
		
//		rest of the code...
		
		String from = "jibrankumari@gmail.com";
		
		String host = "smtp.gmail.com";

        // Get system properties
        Properties properties = System.getProperties();
        System.out.println("PROPERTIES " + properties);

        // Set up the SMTP properties for STARTTLS (port 587)
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");

        // Create the session with authentication
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Replace with your app password from Gmail
                return new PasswordAuthentication("jibrankumari@gmail.com", "xyxv nkwt fmst gwvg");
            }
        });

        session.setDebug(true);

        // Compose the message
        MimeMessage m = new MimeMessage(session);

        try {
            // Set from
            m.setFrom(from);

            // Add recipient
            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set subject
            m.setSubject(subject);

            // Set message
            m.setText(message);

            // Send the message
            Transport.send(m);

            System.out.println("Sent successfully...");
            
            f=true;
            
        } catch (Exception e) {
            e.printStackTrace();
        
   }   
        return f;
		
		
	}

	
}
