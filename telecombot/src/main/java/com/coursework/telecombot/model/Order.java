package com.coursework.telecombot.model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;


import javax.persistence.Entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity(name = "orderinfo")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long chatId;

    private int number;
    private Timestamp orderdate; 

    @Override
    public String toString() {
        String s = new SimpleDateFormat("MM/dd/yyyy").format(getOrderdate());
        return 
            "Номер: " + getNumber() +
            ", Дата: " + s;
    }


}
