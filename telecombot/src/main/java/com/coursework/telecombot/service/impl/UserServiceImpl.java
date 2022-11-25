package com.coursework.telecombot.service.impl;

import java.util.List;

import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coursework.telecombot.model.User;
import com.coursework.telecombot.model.UserRepository;
import com.coursework.telecombot.service.UserService;



@Service
@Component
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;


    @Override
    public User addUser(User user) {
        User savedUser = userRepository.saveAndFlush(user);
        return savedUser;
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
        
    }

    @Override
    public User getById(long id) {
        return userRepository.findById(id).get();
    }

    @Override
    public User editUser(User user) {
        return userRepository.saveAndFlush(user);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public boolean existsUserById(long id) {
        return userRepository.existsById(id);
    }


}
