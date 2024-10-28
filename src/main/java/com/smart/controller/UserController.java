package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
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
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "normal/add_contact_form";
        }

        try {
            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);

            // Processing and uploading file
            if (!file.isEmpty()) {
                contact.setImage(file.getOriginalFilename());
                File saveFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is uploaded");
            }
            else {
            	System.out.println("File is empty");
            	contact.setImage("contact.png");
            }

            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);

            // Use RedirectAttributes to pass the message
            redirectAttributes.addFlashAttribute("message", new Message("Your contact is added! Add more..", "success"));

        } catch (Exception e) {
            System.out.println("ERROR " + e.getMessage());
            e.printStackTrace();

            // Set error message using RedirectAttributes
            redirectAttributes.addFlashAttribute("message", new Message("Something went wrong! Try again..", "danger"));
        }

        return "redirect:/user/add-contact";
    }
    
    //show contacts handler
    @GetMapping("/show-contacts/{page}")
    public String showcontacts(@PathVariable("page") Integer page , Model m,Principal principal) {
    	m.addAttribute("title", "Show User Contacts");
    	
    	String userName = principal.getName();
    	
    	User user = this.userRepository.getUserByUserName(userName);
    	
    	Pageable pageable = PageRequest.of(page, 5);
    	
    	Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
    	
    	m.addAttribute("contacts",contacts);
    	m.addAttribute("currentPage", page);
    	m.addAttribute("totalPages",contacts.getTotalPages());
    	
    	return ("normal/show_contacts");
    }
    
    //showing particular contact details
    
    @GetMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal) 
    {
    	System.out.println("CID " + cId);
    	
    	Optional<Contact> contactOptional =  this.contactRepository.findById(cId);
    	Contact contact = contactOptional.get();
    	
//    	verifying contact and user
    	String userName = principal.getName();
    	User user = this.userRepository.getUserByUserName(userName);
    	
    	if(user.getId() == contact.getUser().getId()) {
    		model.addAttribute("contact",contact);
    		model.addAttribute("title", contact.getName());
    	}
    	
    	
    	return "normal/contact_detail";
    }
    
    //delete contact handler
    
    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cId, Model model, Principal principal, RedirectAttributes redirectAttributes) {

        // Find the contact by its ID
        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        
        if (contactOptional.isPresent()) {
            Contact contact = contactOptional.get();
            
            // Get the currently logged-in user
            String userName = principal.getName();
            User loggedInUser = this.userRepository.getUserByUserName(userName);
            
            // Check if the logged-in user is the owner of the contact
            if (loggedInUser != null && contact.getUser() != null && loggedInUser.getId() == contact.getUser().getId()) {
                
                // Delete the contact image if it exists
                String imgPath = "static/img/" + contact.getImage(); // Assuming the images are in 'static/img/' directory
                Path path = Paths.get(imgPath);

                try {
                    Files.deleteIfExists(path); // Delete the image from the folder
                    System.out.println("Image deleted: " + imgPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    redirectAttributes.addFlashAttribute("message", new Message("Failed to delete contact image!", "danger"));
                }
                
                // Now, delete the contact
                this.contactRepository.delete(contact);
                redirectAttributes.addFlashAttribute("message", new Message("Contact deleted successfully...", "success"));
            } else {
                redirectAttributes.addFlashAttribute("message", new Message("You are not authorized to delete this contact!", "danger"));
            }
        } else {
            redirectAttributes.addFlashAttribute("message", new Message("Contact not found!", "danger"));
        }

        return "redirect:/user/show-contacts/0";
    }

    //open update form handler
    
    @PostMapping("/update-contact/{cid}")
    public String updateform(@PathVariable("cid")Integer cid, Model m)
    {
    	m.addAttribute("title","Update Contact");
    	
    	Contact contact = this.contactRepository.findById(cid).get();
    	
    	m.addAttribute("contact", contact);
    	
    	return "normal/update-form";
    }
    
    //  update contact handler
    @RequestMapping(value = "/process-update",method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model m, RedirectAttributes redirectAttributes,Principal principal )
    {
    	//old contact details
    	Contact oldcontactdetails = this.contactRepository.findById(contact.getcId()).get();
    	
    	try {
    		//image..
    		if(!file.isEmpty()) {
    			
    			//file work..
    			//rewrite
    			
    			//delete old photo
    			
    			File deleteFile = new ClassPathResource("static/img").getFile();
    			File file1 = new File(deleteFile, oldcontactdetails.getImage());
    			file1.delete();
    			
    			//update new photo
    			
    			File savefile = new ClassPathResource("static/img").getFile();
    			
    			Path path = Paths.get(savefile.getAbsolutePath() + File.separator + file.getOriginalFilename());
    			
    			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
    			
    			contact.setImage(file.getOriginalFilename());
    			
    		}
    		else {
    			contact.setImage(oldcontactdetails.getImage());
    		}
    		
    		User user = this.userRepository.getUserByUserName(principal.getName());
    		
    		contact.setUser(user);
    		
    		this.contactRepository.save(contact);
    		
    		redirectAttributes.addFlashAttribute("message", new Message("Contact updated successfully...", "success"));
    		
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	System.out.println("CONTACT NAME " + contact.getName());
    	System.out.println("CONTACT ID " + contact.getcId());
    	return "redirect:/user/" + contact.getcId()+"/contact";
    }
    
    @GetMapping("/profile")
  public String yourProfile(Model model) 
  {
	  model.addAttribute("title", "profile page");
	   return "normal/profile";
  }
    
//    open setting handler
    @GetMapping("/settings")
    public String openSettings()
    {
    	return "normal/settings";
    }
    
}

