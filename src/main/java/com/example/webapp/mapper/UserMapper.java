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
    
    //プロフィール表示用
    User findProfileById(@Param("userId") String userId);

    //ユーザー情報をDBに登録する
    void insert(User user);

    //ユーザーID重複チェック
    boolean existsByUserId(@Param("userId") String userId);

    //フレンドコード重複チェック
    boolean existsByFriendCode(@Param("friendCode") String friendCode);

    // メールアドレスが既に使われているか確認する
    boolean existsByEmail(@Param("userEmail") String userEmail);

    // ログイン用ユーザーIDが既に使われているか確認する
    boolean existsByLoginId(@Param("userLoginId") String userLoginId);
}