package com.example.webapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.webapp.entity.User;

@Mapper
public interface UserMapper {
    // メールアドレスまたはログイン用ユーザーIDでユーザー情報を取得する
    User findByLoginValue(@Param("loginValue") String loginValue);

    // 認証済みユーザーの管理用ユーザーIDでユーザー情報を取得する
    User findById(@Param("userId") String userId);

    //ユーザー情報をDBに登録する
    void insert(User user);  
}