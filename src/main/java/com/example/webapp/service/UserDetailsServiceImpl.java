package com.example.webapp.service;

import com.example.webapp.entity.User;
import com.example.webapp.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Securityのログイン認証専用クラス。
 * 業務ロジックの追加はUserServiceを使うこと。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserMapper UserMapper;

    public UserDetailsServiceImpl(UserMapper UserMapper) {
        this.UserMapper = UserMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String useremail) throws UsernameNotFoundException {
        // useremailパラメータにはメールアドレスかログイン用ユーザーIDが文字列で渡ってくる
        User user = UserMapper.findByLoginValue(useremail);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + useremail);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getUserId()))
                .password(user.getUserPass())   // ハッシュ化済パスワード
                .roles("USER")
                .build();
    }

}
