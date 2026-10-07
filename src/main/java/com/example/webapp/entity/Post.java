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
    //楽曲情報
    private String trackId;
    private String trackName;
    private String artistName;
    private String artworkUrl;
    private String previewUrl;
    private String appleMusicUrl;
    
}