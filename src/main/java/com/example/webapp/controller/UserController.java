package com.example.webapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
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
    public String settings() {
        return "user/settings";
    }

    //ユーザー情報変更画面
    @GetMapping("/settings/account")
    public String settingAccounts() {
        return "user/setting-accounts";
    }

    // プロフィール画面
    /*@GetMapping("/user/{userId}")
    public String userProfile(@PathVariable String userId, Authentication authentication,Model model) {

        // ログインしていない場合
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return "redirect:/user/login";
        }

        // 表示対象のユーザーを取得
        User profileUser = userService.findById(userId);

        // ユーザーが存在しない場合
        if (profileUser == null) {
            return "redirect:/timeline";
        }

        // ログイン中のユーザーを取得
        String loginValue = authentication.getName();
        User loginUser = userService.findByLoginValue(loginValue);

        if (loginUser == null) {
            return "redirect:/user/login";
        }

        // 自分のプロフィールかどうか判定
        boolean isMyPage = loginUser.getUserId().equals(profileUser.getUserId());

        model.addAttribute("profileUser", profileUser);
        model.addAttribute("isMyPage", isMyPage);

        return "user/profile";
    }*/
   @GetMapping("/user/{userId}")
public String userProfile(
        @PathVariable String userId,
        Authentication authentication,
        Model model) {

    System.out.println("受け取ったuserId：" + userId);

    if (authentication == null
            || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getName())) {
        System.out.println("ログインしていないためログイン画面へ");
        return "redirect:/user/login";
    }

    User profileUser = userService.findProfileById(userId);

    System.out.println("取得したprofileUser：" + profileUser);

    if (profileUser == null) {
        System.out.println("ユーザーが見つからないためタイムラインへ");
        return "redirect:/timeline";
    }

    String loginValue = authentication.getName();
    User loginUser = userService.findByLoginValue(loginValue);

    if (loginUser == null) {
        System.out.println("ログイン中のユーザーが見つからない");
        return "redirect:/user/login";
    }

    boolean isMyPage =
            loginUser.getUserId().equals(profileUser.getUserId());

    model.addAttribute("profileUser", profileUser);
    model.addAttribute("isMyPage", isMyPage);

    return "user/profile";
}
}