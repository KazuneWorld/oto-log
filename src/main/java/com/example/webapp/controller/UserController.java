package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
