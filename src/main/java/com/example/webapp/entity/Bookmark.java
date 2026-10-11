package com.example.webapp.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 「自分がブックマークしている投稿」のエンティティ

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bookmark {
    private int bookmarkId;
    private String postId;
    private LocalDateTime postedAt;
    private String postUserId;
    private String userNickname;
    private String userIcon;
    private String trackName;
    private String artistName;
    private String artworkUrl;
    private String previewUrl;
    private String appleMusicUrl;
    private String tagName;
}