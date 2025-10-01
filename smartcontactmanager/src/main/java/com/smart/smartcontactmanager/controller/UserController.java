package com.smart.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.smartcontactmanager.dao.ContactRepository;
import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.Contact;
import com.smart.smartcontactmanager.entities.User;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

//import org.springframework.web.bind.annotation.RequestBody;



@Controller
@RequestMapping("/user")
public class UserController{

    private final DaoAuthenticationProvider authenticationProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    UserController(DaoAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }

    //method for adding common data response
    @ModelAttribute
    public void addCommonData(Model m,Principal principal){
        
        String userName = principal.getName();
        System.out.println("USERNAME "+userName);
        
        //get the user using username(Email)
        User user= userRepository.getUserByUserName(userName);
        System.out.println("USER"+user);
        
        
        m.addAttribute("user", user);
       
    }

    //dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal){
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";

    }

   
    //open add form handler
      @GetMapping("/add-contact")
    public String openAddContactForm(Model model){
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
      
        return "normal/add_contact_form";

    }


    //proccessing add contact form
    @PostMapping("/process-contact")
    public String postMethodName(@ModelAttribute Contact contact, 
    @RequestParam("profileImage") MultipartFile file,
     Principal principal,HttpSession session) {
           
        try{
        String name = principal.getName();

        User user =  this.userRepository.getUserByUserName(name);
       
        //processing and uploading file...
        if(file.isEmpty()){
            //if the file is empty then try out message
            System.out.println("File is Empty"); 
            contact.setImage("contact.jpg");

        }else{
            //file the file to folder and update the name to contact
            contact.setImage(file.getOriginalFilename());
         
            File saveFile=new  ClassPathResource("static/img").getFile();
            
            Path path = Paths.get(saveFile.getAbsoluteFile()+File.separator+file.getOriginalFilename());

            Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
       
            System.out.println("Image is uploded");
        }

          contact.setUser(user);  //--> user ko data base me store kr rha hai...
        user.getContacts().add(contact);
        this.userRepository.save(user);


        System.out.println("DATA "+contact);
        
        System.out.println("Added to data base");


         //message successs....
        // session.setAttribute("message", new Message("your contact is added Successfully!! Add more...","success"));  

        }catch(Exception e){
            System.out.println("ERROR "+e.getMessage());  
            e.printStackTrace();
             //message error....
          // session.setAttribute("message", new Message("something Went wrong!! try again...","danger"));  

        }
        return "normal/add_contact_form";
    }
    
    //show contact handler
    //per page=5[n]
    //current page = 0[page]

    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page , Model m , Principal principal){
        m.addAttribute("title", "View contacts");
        //contact ki list ko bhejni hai

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

          PageRequest pageable=PageRequest.of(page,3);

        Page<Contact>contacts = this.contactRepository.findContactByUser(user.getId(),pageable);
         m.addAttribute("contacts", contacts);
         m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());
        return "normal/show_contacts";
    }

    //showing particular contact details
    @GetMapping("/{cId}/contact")
    public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal){
        System.out.println("CID "+cId);

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();
       // model.addAttribute("contact",contact);

        //contact ki details user ko hi deni hai
        //secure the contact details
        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);
        
            if(user.getId() == contact.getUser().getId()){
                model.addAttribute("contact", contact);
                model.addAttribute("title", contact.getName());
            }
       

        return "normal/contact_details";
    }

    //delete contact handler
    @GetMapping("/delete/{cId}")
    @Transactional
    public String deleteContact(@PathVariable("cId") Integer cId, HttpSession session, Principal principal){
        System.out.println("CID "+cId);
        Contact contact = this.contactRepository.findById(cId).get();

      // System.out.println("CONTACT "+contact.getcId());
        
       contact.setUser(null);//  to break the relationship btween user and contact
        User user = this.userRepository.getUserByUserName(principal.getName());
      
         user.getContacts().remove(contact);

        this.userRepository.save(user);

        this.contactRepository.delete(contact);
       
       System.out.println("DELETED");
        //session.setAttribute("message",new Message("Contact deleted successfully...","success"));
        return "redirect:/user/show-contacts/0";
    }

    //open update form handler
    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model m){
        m.addAttribute("title", "Update Contact");
        Contact contact = this.contactRepository.findById(cId).get();
        m.addAttribute("contact", contact);
        return "normal/update_form";
    }

    //update contact handler--------------------------------------
    @RequestMapping(value="/process-update", method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") 
    MultipartFile file, Model m , HttpSession session,Principal principal)
    {

        try{
            //old contact details
            Contact oldContact = this.contactRepository.findById(contact.getcId()).get();
            //imge...
            if(!file.isEmpty()){
                //file work...
                //delete old photo
                File deleteFile=new  ClassPathResource("static/img").getFile();
               
                File file1=new File(deleteFile,oldContact.getImage());
               
                file1.delete();
               
                System.out.println("Old Image Deleted");

                //upload new photo---------------------
                File saveFile=new  ClassPathResource("static/img").getFile();
               
                Path path = Paths.get(saveFile.getAbsoluteFile()+File.separator+file.getOriginalFilename());
               
                Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
               
                contact.setImage(file.getOriginalFilename());

            }else{
                contact.setImage(oldContact.getImage());
            }


            //contact ki user ko set krna hai
           
            User user = this.userRepository.getUserByUserName(principal.getName());
           
            contact.setUser(user);
           
            this.contactRepository.save(contact);
            //message success...
           
           // session.setAttribute("message", new Message("Your contact is updated...","success"));

        }catch(Exception e){
            e.printStackTrace();

        }
      System.out.println("CONTACT  Name"+contact.getName());
      
        System.out.println("CONTACT ID"+contact.getcId());

    return "redirect:/user/"+contact.getcId()+"/contact";
}


   //your profile handler
   @GetMapping("/profile")
    public String yourProfile(Model m){
         m.addAttribute("title", "Profile Page");
         return "normal/profile";
    }

}

    