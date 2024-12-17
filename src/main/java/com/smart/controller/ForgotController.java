package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailservice;
	
	@Autowired
	private UserRepository userRepository;

//	email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,RedirectAttributes redirectAttributes,HttpSession session)
	{
		
		System.out.println("EMAIL " + email);
		
//		generating otp of 4 digit
		
		
		
		int otp = random.nextInt(99999);
		
		System.out.println("OTP "+otp);
		
//		write code for send otp to email...
		
		String subject="OTP From SCM";
		String message=""
					+	  "<div style='border:1px solid #e2e2e2; padding:20px'> "
				     +    "<h1>"
					+      "OTP is"
				     +      "<b>"+otp
				     +     "</n>"
				     +      "</h1>"
				     +      "</div>";
		String to = email;
		
	    boolean flag =	this.emailservice.sendEmail(subject, message, to);
		
	    if(flag)
	    {
	    	
	    	session.setAttribute("otp", otp);
	    	session.setAttribute("email", email);
	    	
	    	return "varify_otp";
	    }else {
	    	
	    	redirectAttributes.addFlashAttribute("message", new Message("Please check your email id !!", "danger"));
	    	return "forgot_email_form";
	    }
	    
		
	}
	
//	verify otp
	@PostMapping("/varify-otp") // Updated path to match your file name
	public String varifyOtp(@RequestParam("otp") int otp, HttpSession session, RedirectAttributes redirectAttributes) {
	    // Retrieve the OTP and email from session
	    Integer myOtp = (Integer) session.getAttribute("myotp");
	    String email = (String) session.getAttribute("email");

	    // Check if OTP matches
	    if (myOtp != null && myOtp == otp) {
	        // Fetch the user by email
	        User user = this.userRepository.getUserByUserName(email);

	        if (user == null) {
	            // User not found with this email
	            redirectAttributes.addFlashAttribute("message", new Message("User does not exist with this email", "danger"));
	            return "redirect:/forgot_email_form"; // Redirect to the forgot email form
	        } else {
	            // OTP verified; show password change form
	            redirectAttributes.addFlashAttribute("message", new Message("OTP verified! Please set a new password.", "success"));
	            return "password_change_form"; // Show form to reset password
	        }
	    } else {
	        // OTP verification failed
	        redirectAttributes.addFlashAttribute("message", new Message("You have entered the wrong OTP!", "danger"));
	        return "redirect:/varify_otp"; // Updated the path here to match your file
	    }
	}
}