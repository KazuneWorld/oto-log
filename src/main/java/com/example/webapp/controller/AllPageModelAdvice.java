package com.example.webapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.webapp.entity.User;
import com.example.webapp.mapper.UserMapper;

//@ControllerAdviceを付与することで、全てのControllerで共通して使用されるModelAttributeを定義
@ControllerAdvice
public class AllPageModelAdvice {
    
    private final UserMapper userMapper;

    AllPageModelAdvice(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    // フッターで使用するログイン中ユーザー情報をModelAttributeとして提供
    @ModelAttribute("userInfo")
    public User userInfo(Authentication authentication) {
    	// ログインしていない場合はnullを返す
        boolean isLoggedIn = authentication != null
                && !"anonymousUser".equals(authentication.getPrincipal());

        if (!isLoggedIn) {
            return null;
        }
     // ログインしている場合はユーザーIDを取得して、ユーザー情報を返す
        String userId = authentication.getName(); 
        return userMapper.findById(userId);
    }
}