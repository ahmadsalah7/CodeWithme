package com.codewithme.compiler.entity;

import java.util.Date;

import lombok.*;

@Data
public class Comment {
    private String username;
    private String content;
    private Date timestamp;

    @Override
    public String toString() {
        return "Commented by: "+username+ "at" +timestamp.toString();
    }
}
