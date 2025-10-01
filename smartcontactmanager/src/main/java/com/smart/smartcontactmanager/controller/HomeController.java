package com.smart.smartcontactmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.User;
import com.smart.smartcontactmanager.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
private String  home(Model model){
    model.addAttribute("title","Home - Smart Contact Manager");
    return "home";
}
    
    @RequestMapping("/about")
    private String  about(Model model){
        model.addAttribute("title","About - Smart Contact Manager");
        return "about";
    }


   //handler for custom login
    @GetMapping("/signin")
    private String  login(Model model){
        model.addAttribute("title","Login page");
        return "login";
    }
    
    //handler for signup page
     @RequestMapping("/signup")
    private String  signup(Model model){
        model.addAttribute("title","Register- Smart Contact Manager");
        model.addAttribute("user",new User());
        return "signup";
    }

    //handler for registering user
     
    @RequestMapping(value="/do_register" , method= RequestMethod.POST)
    private String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result1 , 
    @RequestParam(value = "agreement",defaultValue="false")boolean agreement , 
    Model model,HttpSession session){
       

        try {
            
             if(!agreement){
            System.out.println("YOu have not agreed the terms and conditions");
            throw new Exception("YOu have not agreed the terms and conditions");
        }


        if(result1.hasErrors())
        {
            System.out.println("ERROR" + result1.toString());
            model.addAttribute("user", user);
            return "signup";
        }

        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setImageUrl("default.png");
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        System.out.println("Agreement"+agreement);
        System.out.println("USER"+user);
        

       User result= this.userRepository.save(user);

       model.addAttribute("user",new User());
       session.setAttribute("message", new Message("Successfully Registered","alert-success"));

             return"signup";


        } catch (Exception e) {
           
            e.printStackTrace();
            model.addAttribute("user",user);
            session.setAttribute("message", new Message("Something Went wrong!!"+e.getMessage(),"alert-danger"));

             return"signup";
        }
       
       
    }
   

}
