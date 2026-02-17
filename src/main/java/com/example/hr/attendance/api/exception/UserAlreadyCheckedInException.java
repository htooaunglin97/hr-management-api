package com.example.hr.attendance.api.exception;

public class UserAlreadyCheckedInException extends RuntimeException{

    public UserAlreadyCheckedInException(String message) {
        super(message);
    }

}
