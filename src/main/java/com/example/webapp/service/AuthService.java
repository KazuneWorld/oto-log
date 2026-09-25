package com.example.webapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import com.example.webapp.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {

    @Autowired
    private UserDetailsService userDetailsService;

    // 指定したユーザーでログイン状態にする(自動ログイン)
    public void login(User user, HttpServletRequest request) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // セッションに明示的に保存する(これが無いと次のリクエストで認証情報が消える)
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}