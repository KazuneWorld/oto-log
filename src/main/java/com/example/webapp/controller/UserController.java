package com.example.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.webapp.entity.User;
import com.example.webapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

// UserController
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

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
                          HttpServletRequest request) {

        //パスワードと確認用パスワードが一致しているかチェック
        if(!user.getUserPass().equals(userPassConfirm)){
            return "user/signup";
        }

        //ユーザー情報をDBに登録
        userService.insert(user);

        //登録したユーザーで自動ログイン状態にする
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        //セッションに明示的に保存する(これが無いと次のリクエストで認証情報が消える)
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return "redirect:/timeline";
    }
}