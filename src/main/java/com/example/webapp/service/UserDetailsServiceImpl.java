package com.example.webapp.service;

import com.example.webapp.entity.User;
import com.example.webapp.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserMapper UserMapper;

    @Override
    public UserDetails loadUserByUsername(String useremail) throws UsernameNotFoundException {
        // useremailパラメータにはメールアドレスが文字列で渡ってくる
        User user = UserMapper.findByUserId(useremail);

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getUserEmail()))
                .password(user.getUserPass())   // 平文のまま(NoOpPasswordEncoder前提)
                .build();
    }

}
