package com.coursework.telecombot.service;

import java.util.List;

import org.jvnet.hk2.annotations.Service;

import com.coursework.telecombot.model.User;


@Service
public interface UserService{

    User addUser(User user);
    void deleteUser(long id);
    User getById(long id);
    User editUser(User user);
    List<User> getAll();
    boolean existsUserById(long id);

    

    

}
