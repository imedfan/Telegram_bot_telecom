package com.coursework.telecombot.service.impl;

import java.util.List;


import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.coursework.telecombot.model.Order;
import com.coursework.telecombot.model.OrderRepository;

import com.coursework.telecombot.service.OrderService;

@Service
@Component
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Order addOrder(Order order) {
        Order saveOrder = orderRepository.saveAndFlush(order);
        return saveOrder;
    }

    @Override
    public void deleteOrder(long id) {
        orderRepository.deleteById(id);        
    }

    @Override
    public Order getById(long id) {
        return orderRepository.findById(id).get();
    }


    @Override
    public Order editOrder(Order order) {
        return orderRepository.saveAndFlush(order);
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public boolean existsUserById(long id) {
        return orderRepository.existsById(id);
    }
    
    
}
