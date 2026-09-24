package com.example.webapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Song {

    private String songId;
    private String trackId;
    private String trackName;
    private String artistName;
    private String artworkUrl;
    private String previewUrl;
    private String appleMusicUrl;

    
}