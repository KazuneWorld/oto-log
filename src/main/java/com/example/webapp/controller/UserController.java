package com.example.webapp.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;

// UserController
@Controller
public class UserController {


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

    
}
