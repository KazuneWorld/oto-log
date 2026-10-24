package com.example.webapp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.webapp.entity.User;
import com.example.webapp.mapper.UserMapper;
import com.example.webapp.service.NotificationService;

//@ControllerAdviceを付与することで、全てのControllerで共通して使用されるModelAttributeを定義
@ControllerAdvice(basePackages = "com.example.webapp.controller")
public class AllPageModelAdvice {

    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public AllPageModelAdvice(UserMapper userMapper, NotificationService notificationService) {
        this.userMapper = userMapper;
        this.notificationService = notificationService;
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

    /** フッターの通知バッジ用の未読件数。ログインしていない場合は 0 */
    @ModelAttribute("unreadNotificationCount")
    public int unreadNotificationCount(Authentication authentication) {
        if (authentication == null || "anonymousUser".equals(authentication.getPrincipal())) {
            return 0;
        }
        String userId = authentication.getName();
        return userId == null ? 0 : notificationService.countUnread(userId);
    }
}