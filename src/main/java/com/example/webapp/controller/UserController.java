package com.example.webapp.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;

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

    @Autowired
    private PasswordEncoder passwordEncoder;
    

    //最初の画面
    @GetMapping("/")
    public String index(HttpServletRequest request) {
        // もしすでにログイン中（Spring Securityの認証情報がある）かチェック
        if (request.getUserPrincipal() != null) {
            // ログイン済みの人はタイムラインへ強制送還
            return "redirect:/timeline";
        }
        
        return "user/index";
    }

    //ログイン画面(表示のみ、認証処理はSpring Securityが自動でやる)
    //（Spring Security逆バージョン：ログイン済みの人はタイムラインへ弾く）
    @GetMapping("/user/login")
    public String login(HttpServletRequest request) {
         //もしすでにログイン中（Spring Securityの認証情報がある）かチェック
        if (request.getUserPrincipal() != null) {
             //ログイン済みの人はタイムラインへ強制送還
            return "redirect:/timeline";
        }
        
        return "user/login";
    }

    //タイムライン画面
    @GetMapping("/timeline")
    public String timeline() {
        return "post/timeline";
    }


    // 会員登録フォームを表示（Spring Security逆バージョン：ログイン済みの人はタイムラインへ弾く）
    @GetMapping("/user/signup")
    public String signupForm(@ModelAttribute User user, HttpServletRequest request) {
        // すでにログイン状態かチェック
        if (request.getUserPrincipal() != null) {
            // ログイン済みの人はタイムラインへ強制送還（逆バージョン）
            return "redirect:/timeline";
        }
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
    public String settings(HttpServletRequest request, Model model) {

        if (request.getUserPrincipal() == null) {
           return "redirect:/user/login";
        }

        String userId = request.getUserPrincipal().getName();
        User user = userService.findById(userId);

        if (user == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", user);

        return "user/settings";
    }

    // ユーザー情報変更画面
    @GetMapping("/settings/account")
    public String settingAccountsFrom(HttpServletRequest request, Model model) {

        if (request.getUserPrincipal() == null) {
            return "redirect:/user/login";
        }

        String userId = request.getUserPrincipal().getName();
        User user = userService.findById(userId);

        if (user == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", user);

        return "user/setting-accounts";
    }

    // ユーザー情報変更処理
    @PostMapping("/user/settings")
    public String settingAccount(
        @ModelAttribute User user,
        @RequestParam String username,
        @RequestParam String loginid,
        @RequestParam String email,
        @RequestParam String currentPassword,
        @RequestParam String newPassword,
        @RequestParam("password-confirm") String passwordConfirm,
        HttpServletRequest request,
        Model model) {

        // ログイン中のユーザー情報を取得
        String loginUserId = request.getUserPrincipal().getName();
        User currentUser = userService.findById(loginUserId);

        // 現在のパスワードが正しいか確認
            if (!passwordEncoder.matches(
                currentPassword, currentUser.getUserPassHash())) {
            model.addAttribute("errorMessage", "現在のパスワードが正しくありません。");
            model.addAttribute("user", currentUser);
            return "user/setting-accounts";
        }

        // 新しいパスワードと確認用パスワードの一致チェック
        if (!newPassword.equals(passwordConfirm)) {
            model.addAttribute("errorMessage", "新しいパスワードが一致しません。");
            model.addAttribute("user", currentUser);
            return "user/setting-accounts";
        }

        // ログインIDの重複チェック（自分自身は除外）
        if (!loginid.equals(currentUser.getUserLoginId())
                && userService.isLoginIdTaken(loginid)) {
            model.addAttribute("errorMessage", "このログインIDは既に使用されています。");
            model.addAttribute("user", currentUser);
            return "user/setting-accounts";
        }

        // 変更するユーザー情報を設定
        currentUser.setUserNickname(username);
        currentUser.setUserLoginId(loginid);
        currentUser.setUserEmail(email);

        // 新しいパスワードが入力されている場合だけ変更
        boolean changePassword = newPassword != null && !newPassword.isBlank();
        if (changePassword) {
            currentUser.setUserPass(newPassword);
        }

        // DBを更新
        userService.update(currentUser, changePassword);

        return "redirect:/settings";
    }

    
}