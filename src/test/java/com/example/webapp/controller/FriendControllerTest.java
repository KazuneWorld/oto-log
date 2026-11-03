package com.example.webapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import com.example.webapp.entity.Notification;
import com.example.webapp.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class FriendControllerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private FriendController friendController;

    @Test
    void 通知一覧を取得して未読件数を0にして表示する() {
        List<Notification> notifications = List.of(new Notification());
        when(authentication.getName()).thenReturn("user-1");
        when(notificationService.getListAndMarkAllAsRead("user-1")).thenReturn(notifications);

        String view = friendController.notifications(model, authentication);

        assertEquals("friend/notifications", view);
        verify(model).addAttribute("notifications", notifications);
        verify(model).addAttribute("unreadNotificationCount", 0);
    }
}
