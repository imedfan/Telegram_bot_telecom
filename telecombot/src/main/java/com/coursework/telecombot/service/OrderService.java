package com.coursework.telecombot.service;

import java.util.List;

import org.jvnet.hk2.annotations.Service;

import com.coursework.telecombot.model.Order;

@Service
public interface OrderService {

    Order addOrder(Order order);
    void deleteOrder(long id);
    Order getById(long id);
    Order editOrder(Order order);
    List<Order> getAll();
    boolean existsUserById(long id);
    
}
