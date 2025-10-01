package com.smart.smartcontactmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.smartcontactmanager.dao.UserRepository;
import com.smart.smartcontactmanager.entities.User;

public class UserDetailServices  implements UserDetailsService{


    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       
        //feching user from datebase

        User user = userRepository.getUserByUserName(username);
         
        if(user==null){
            throw new UsernameNotFoundException("could not found user!!");

        }

        CustomUserDetail customUserDetail = new CustomUserDetail(user);

    return customUserDetail;

    }
    
}
