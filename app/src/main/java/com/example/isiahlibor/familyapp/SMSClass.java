package com.example.isiahlibor.familyapp;

import java.io.Serializable;

public class SMSClass implements Serializable {

    private String number;
    private String message;

    private static final long serialVersionUID = 1L;

    public SMSClass() {
        this.number = "";
        this.message = "";
    }

    public SMSClass(String number, String message) {
        this.number = number;
        this.message = message;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
