package com.example.webapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


import com.example.webapp.entity.MoodTag;
import com.example.webapp.mapper.PostMapper;

import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.webapp.service.PostService;

// UserController
@Controller
public class UserController {
    @Autowired
    private PostMapper postMapper;

    @Autowired 
    private PostService postService;

    //最初の画面
    @GetMapping("/")
    public String index() {
        return "user/index";
    }

    //ログイン画面(表示のみ、認証処理はSpring Securityが自動でやる)
    @GetMapping("/user/login")
    public String login() {
        return "user/login";
    }

    //タイムライン画面
    @GetMapping("/timeline")
    public String timeline() {
        return "post/timeline";
    }


    //投稿後のタイムライン画面
    @PostMapping("/timeline")
    public String postTimeline(@RequestParam("userId") String userId, @RequestParam("moodTagId") int moodTagId) {
        String postId = postService.generateRandomString(12);
        postMapper.insertPostId(postId, userId, moodTagId);
        return "post/timeline";
    }

    // 設定画面
    @GetMapping("/settings")
    public String settings() {
        return "user/settings";
    }

    //ユーザー情報変更画面
    @GetMapping("/settings/account")
    public String settingAccounts() {
        return "user/setting-accounts";
    }
    
}
