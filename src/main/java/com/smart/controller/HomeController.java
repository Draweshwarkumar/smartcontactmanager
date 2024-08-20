package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String Home(Model model) {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    @GetMapping("/signup")
    public String signup(Model model, HttpSession session) {
        model.addAttribute("title", "Register - Smart Contact Manager");
        model.addAttribute("user", new User());

        // Check if there's a message in the session and pass it to the model
        Message message = (Message) session.getAttribute("message");
        if (message != null) {
            model.addAttribute("message", message);
            session.removeAttribute("message"); // Remove it after adding to the model
        }

        return "signup";
    }

    @PostMapping("/do_register")
    public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult Result, @RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model, HttpSession session) {
       
    	try {
    	
    	if (!agreement) {
            session.setAttribute("message", new Message("You must accept the terms and conditions.", "alert-danger"));
            model.addAttribute("user", user);
            return "signup";
//    		System.out.println("You have not agreed the terms and conditions");
//    		throw new Exception("You have not agreed the terms and conditions");
        }
    	
    	if(Result.hasErrors()) {
    		System.out.println("ERROR" + Result.toString());
    		model.addAttribute("user",user);
    		return "signup";
    	}

        
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");

            User result = this.userRepository.save(user);

            model.addAttribute("user", new User());
            session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
            return "signup";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("user", user);
            session.setAttribute("message", new Message("Something Went Wrong !!" + e.getMessage(), "alert-danger"));
            return "signup";
        }
    }
}

//	@Autowired
//private UserRepository userRepository;
//
//@GetMapping("/test")
//@ResponseBody
//public String test() {
//	
//	User user = new User();
//	user.setName("Shivam jha");
//	user.setEmail("shivam@gmail.com");
//	
//	Contact contact  = new Contact();
//	
//	user.getContacts().add(contact);
//	
//	userRepository.save(user);
//	
//	return "working";
//}
//
//}


