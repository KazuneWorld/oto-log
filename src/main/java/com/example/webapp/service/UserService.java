package com.example.webapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.webapp.entity.User;
import com.example.webapp.mapper.UserMapper;

@Service 
public class UserService {

     @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ユーザー登録
    public void insert(User user) {
        //ランダムにユーザーIDを生成
        user.setUserId(generateUserId());
        //ランダムにフレンドコードを生成
        user.setFriendCode(generateFriendCode());
        //パスワードをハッシュ化
        user.setUserPassHash(passwordEncoder.encode(user.getUserPass()));
        //ユーザー情報をDBに登録
        userMapper.insert(user);
    }
}