package com.example.webapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.webapp.entity.User;
import com.example.webapp.mapper.UserMapper;

@Service 
public class UserService {

     @Autowired
    private UserMapper userMapper;

    // ユーザー登録
    public void insert(User user) {
        //ランダムにユーザーIDを生成
        user.setUser_id(generateUserId());
        //ランダムにフレンドコードを生成
        user.setfriend_code(generateFriendCode());
        //パスワードをハッシュ化
        user.setUserPassHash(passwordEncoder.encode(user.getUserPass()));
        //ユーザー情報をDBに登録
        userMapper.insert(user);
    }
    
}
