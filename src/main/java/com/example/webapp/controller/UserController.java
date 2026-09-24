package com.example.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.webapp.mapper.PostMapper;

// UserController
@Controller
public class UserController {
    @Autowired
    private PostMapper postMapper;

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
    public String postTimeline(){
        
        return "post/timeline";
    }
    
}
