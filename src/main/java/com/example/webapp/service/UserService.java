package com.example.webapp.service;

import java.security.SecureRandom;

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

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int USER_ID_LENGTH = 12;
    private static final int FRIEND_CODE_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

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

    //ランダムなユーザーIDを生成(12桁の英数字)
    private String generateUserId() {
        return generateRandomString(USER_ID_LENGTH);
    }

    //ランダムなフレンドコードを生成(8桁の英数字)
    private String generateFriendCode() {
        return generateRandomString(FRIEND_CODE_LENGTH);
    }

    //指定した桁数のランダムな文字列を生成
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}