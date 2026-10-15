package com.example.webapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;



@Data
@NoArgsConstructor
@AllArgsConstructor

public class TimelinePost {

    private String postId;
    private String userId;
    private int songId;
    private int moodTagId;
    private  Date postedAt;

    
    //投稿表示に必要なユーザ情報
    private String userNickname;
    private String userIcon;

    //投稿表示に必要な音楽情報
    private Long trackId;
    private String trackname;
    private String artworkUrl;
    private String previewUrl;
    private String appleMusicUrl;
    
    //投稿表示に必要なタグ情報
    private String tagName;



    
}