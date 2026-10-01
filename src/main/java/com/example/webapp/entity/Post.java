package com.example.webapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class Post {

    private String postId;
    private String userId;
    private int songId;
    private int moodTagId;
    private  Date postedAt;

    
}