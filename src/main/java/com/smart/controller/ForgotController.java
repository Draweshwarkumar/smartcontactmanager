package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailservice;

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
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession session,RedirectAttributes redirectAttributes)
	{
		Integer myOtp =(int) session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		if(myOtp == otp)
		{
//			password change form
			return "password_change_form";
		}
		else {
			redirectAttributes.addFlashAttribute("message", new Message("You have entered wrong otp !!", "danger"));
			return "verify_otp";
		}
	}
}
