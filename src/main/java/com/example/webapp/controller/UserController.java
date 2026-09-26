package com.example.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.webapp.entity.User;
import com.example.webapp.service.AuthService;
import com.example.webapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;


// UserController
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

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


    //会員登録フォームを表示
    @GetMapping("/user/signup")
    public String signupForm(@ModelAttribute User user) {
        return "user/signup";
    }

    //会員登録処理
    @PostMapping("/user/signup")
    public String signup(@ModelAttribute User user, @RequestParam String userPassConfirm,
                          HttpServletRequest request, Model model) {

        //パスワードと確認用パスワードが一致しているかチェック
        if(!user.getUserPass().equals(userPassConfirm)){
            model.addAttribute("errorMessage", "パスワードが一致しません。");
            return "user/signup";
        }

        //メールアドレスの重複チェック
        if(userService.isEmailTaken(user.getUserEmail())){
            model.addAttribute("errorMessage", "このメールアドレスは既に使用されています。");
            return "user/signup";
        }

        //ログイン用ユーザーIDの重複チェック
        if(userService.isLoginIdTaken(user.getUserLoginId())){
            model.addAttribute("errorMessage", "このログインIDは既に使用されています。");
            return "user/signup";
        }

        //ユーザー情報をDBに登録
        userService.insert(user);

        //登録したユーザーで自動ログイン状態にする
        authService.login(user, request);

        return "redirect:/timeline";
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