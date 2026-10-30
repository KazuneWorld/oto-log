package com.example.webapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.webapp.entity.Notification;
import com.example.webapp.mapper.NotificationMapper;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void フレンド申請通知を登録してイベントを発行する() {
        when(notificationMapper.countSameUnreadWithoutPost("receiver", "requester", 0))
                .thenReturn(0);

        notificationService.notifyFriendRequest("requester", "receiver");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();
        assertEquals("receiver", notification.getRecipientId());
        assertEquals("requester", notification.getSenderId());
        assertEquals(0, notification.getNotificationType());
        verify(eventPublisher).publishEvent(
                new NotificationCreatedEvent("receiver", "requester", 0, null));
    }

    @Test
    void 自分自身への通知は登録しない() {
        notificationService.notifyFriendRequest("same-user", "same-user");

        verify(notificationMapper, never()).insert(any(Notification.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 未読の重複通知は登録しない() {
        when(notificationMapper.countSameUnreadWithoutPost("receiver", "requester", 0))
                .thenReturn(1);

        notificationService.notifyFriendRequest("requester", "receiver");

        verify(notificationMapper, never()).insert(any(Notification.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void 通知一覧を取得して先頭までを既読にする() {
        Notification first = new Notification();
        first.setNotificationId(10);
        Notification second = new Notification();
        second.setNotificationId(9);
        when(notificationMapper.findByRecipient("receiver", 30))
                .thenReturn(List.of(first, second));

        List<Notification> result = notificationService.getListAndMarkAllAsRead("receiver");

        assertEquals(List.of(first, second), result);
        verify(notificationMapper).markAsReadUpTo("receiver", 10);
    }

    @Test
    void 存在しない投稿へのリアクション通知は登録しない() {
        when(notificationMapper.findPostOwnerId("missing-post")).thenReturn(null);

        notificationService.notifyReaction("missing-post", "reactor");

        verify(notificationMapper, never()).insert(any(Notification.class));
        verify(eventPublisher, never()).publishEvent(any());
    }
}
