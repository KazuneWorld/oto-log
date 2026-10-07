package com.example.webapp.service;


import com.example.webapp.entity.Post;
import com.example.webapp.mapper.PostMapper;


import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;


@Service
public class PostService {
    
    @Autowired 
    private PostMapper postMapper;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final int POST_ID_LENGTH = 12;
    //private String  testPostId = "POST00000001";      重複確認のテストデータ
    
    public void insertPost(String userId, Post post) {
        String postId = generateUniquePostId();
        post.setUserId(userId);
        post.setPostId(postId);
        postMapper.insertPost(post);
    }

    //指定した桁数のランダムな文字列を生成
    public String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    //重複しない投稿IDを生成
    private String generateUniquePostId() {
        String postId;
        do {
            postId =  generateRandomString(POST_ID_LENGTH);   //testPostId;    ←重複確認のテストデータ
        } while (postMapper.existsByPostId(postId));
        return postId;
    }
    

}
