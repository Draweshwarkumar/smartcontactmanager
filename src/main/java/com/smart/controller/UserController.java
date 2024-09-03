package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    // Method for adding common data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("USERNAME: " + userName);

        User user = userRepository.getUserByUserName(userName);
        model.addAttribute("user", user);
    }

    // Dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

    // Open add form handler
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    // Processing add contact form
    @PostMapping("/process-contact")
    public String processContact(
            @ModelAttribute("contact") @Valid Contact contact,
            @RequestParam("profileImage") MultipartFile file,
            BindingResult result,
            Principal principal,
            Model model) {

        if (result.hasErrors()) {
            return "normal/add_contact_form"; // Return to the form if there are validation errors
        }
       
        try {
        String name = principal.getName();
        User user = this.userRepository.getUserByUserName(name);
        
    //    processing and uploading file
        
        if(file.isEmpty()) {
        	//if the file is empty then try our message
        	System.out.println("File is empty");
        }
        else {
        	//file the file to folder and update the name to contact
        	contact.setImage(file.getOriginalFilename());
        	
        	File saveFile = new ClassPathResource("static/img").getFile(); 
        	
        	Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
        	
        	Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
        	
        	System.out.println("Image is uploaded");
        }
        
        contact.setUser(user);
        user.getContacts().add(contact);
        this.userRepository.save(user);

        System.out.println("DATA: " + contact);
        System.out.println("Added to database");
        
        }catch(Exception e) {
        	System.out.println("ERROR " + e.getMessage());
        	e.printStackTrace();
        }
        
        return "redirect:/user/add-contact"; //Redirect to the dashboard or another appropriate page
    }
}
