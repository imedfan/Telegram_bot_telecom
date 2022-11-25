package com.coursework.telecombot.model;
import java.sql.Timestamp;

import javax.persistence.Entity;

import javax.persistence.Id;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Setter
@Entity(name = "userdata")
public class User {
    
    @Id
    private Long chatId;

    private String firstname;
    private String lastname;
    private String username;
    private Timestamp dataregister;
    private String email;
    private String phone;
    private String tariff;
    private Integer balance;

    // @OneToMany(mappedBy = "user")
    // private List<Order> orders; 

}
