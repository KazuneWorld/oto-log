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

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int USER_ID_LENGTH = 12;
    private static final int FRIEND_CODE_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    // ユーザー登録
    public void insert(User user) {
        //ランダムにユーザーIDを生成(重複チェック付き)
        user.setUserId(generateUniqueUserId());
        //ランダムにフレンドコードを生成(重複チェック付き)
        user.setFriendCode(generateUniqueFriendCode());
        //パスワードをハッシュ化
        user.setUserPassHash(passwordEncoder.encode(user.getUserPass()));
        //ユーザー情報をDBに登録
        userMapper.insert(user);
    }

    // メールアドレスが既に使われているか確認する
    public boolean isEmailTaken(String userEmail) {
        return userMapper.existsByEmail(userEmail);
    }

    // ログイン用ユーザーIDが既に使われているか確認する
    public boolean isLoginIdTaken(String userLoginId) {
        return userMapper.existsByLoginId(userLoginId);
    }

    //指定した桁数のランダムな文字列を生成
    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    //重複しないユーザーIDを生成
    private String generateUniqueUserId() {
        String userId;
        do {
            userId = generateRandomString(USER_ID_LENGTH);
        } while (userMapper.existsByUserId(userId));
        return userId;
    }

    //重複しないフレンドコードを生成
    private String generateUniqueFriendCode() {
        String friendCode;
        do {
            friendCode = generateRandomString(FRIEND_CODE_LENGTH);
        } while (userMapper.existsByFriendCode(friendCode));
        return friendCode;
    }
}