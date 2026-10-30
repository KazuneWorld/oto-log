package com.example.webapp.service;

import com.example.webapp.entity.Notification;
import com.example.webapp.entity.NotificationType;
import com.example.webapp.mapper.NotificationMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知の入口。
 *
 * 通知を作りたい機能(フレンド申請・リアクション・投稿など)は、
 * この Service の notify〇〇〇() を1行呼ぶだけでよい。
 * 通知の宛先や送り主の向きはここで吸収しているので、呼び出し側は意識しなくてよい。
 *
 * 全メソッドに共通のルール:
 *  ・自分自身には通知しない
 *  ・同じ内容の未読通知がすでにある場合は、重複して作らない
 *    (例: リアクションの絵文字を何度も押し直しても、通知は1件のまま)
 *
 * 通知を登録すると NotificationCreatedEvent を発行する。プッシュ通知は、このイベントを
 * 受け取って送られる(この Service は、プッシュ通知のことを知らなくてよい)。
 * 自分宛て・重複で登録されなかった場合は、イベントも発行されない(プッシュも送られない)。
 *
 * クラス全体に @Transactional を付けているので、呼び出し元がトランザクション中なら
 * その一部として動く(例: フレンド申請の登録と通知の登録が、片方だけ成功することはない)。
 */
@Service
@Transactional
public class NotificationService {

    // 通知一覧で表示する最大件数
    private static final int LIST_LIMIT = 30;

    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    public NotificationService(NotificationMapper notificationMapper, ApplicationEventPublisher eventPublisher) {
        this.notificationMapper = notificationMapper;
        this.eventPublisher = eventPublisher;
    }

    // ============================================
    // 通知を作る(各機能から呼んでください)
    // ============================================

    /** フレンド申請が届いた。申請された側(receiver)に通知する */
    public void notifyFriendRequest(String requesterId, String receiverId) {
        createNotification(NotificationType.FRIEND_REQUEST, receiverId, requesterId, null);
    }

    /** 申請が承認され、フレンドになった。申請した側(requester)に通知する */
    public void notifyFriendAccepted(String requesterId, String accepterId) {
        createNotification(NotificationType.FRIEND_ACCEPTED, requesterId, accepterId, null);
    }

    /** 投稿にリアクションが付いた。投稿の持ち主に通知する */
    public void notifyReaction(String postId, String reactorId) {
        String ownerId = notificationMapper.findPostOwnerId(postId);
        if (ownerId == null) {
            return; // 存在しない投稿
        }
        createNotification(NotificationType.REACTION, ownerId, reactorId, postId);
    }

    /** 投稿された。「この人の投稿を通知する」に設定しているフレンド全員に通知する */
    public void notifyFriendPosted(String postId) {
        String posterId = notificationMapper.findPostOwnerId(postId);
        if (posterId == null) {
            return; // 存在しない投稿
        }
        for (String friendId : notificationMapper.findNotifyingFriendIds(posterId)) {
            createNotification(NotificationType.FRIEND_POSTED, friendId, posterId, postId);
        }
    }

    // ============================================
    // 通知を読む(通知一覧画面・フッターから呼ぶ)
    // ============================================

    /**
     * 通知一覧を取得し、一覧を開いた時点までの通知をすべて既読にする。
     *
     * 返す一覧の各通知の read は「開く前の状態」のまま(今回開いた時点で未読だったものを強調表示できる)。
     * 一覧の取得件数は30件に制限しているが、既読化は取得できなかった古い通知も含めて行う。
     * この処理の後に届いた通知は既読にしない。
     */
    public List<Notification> getListAndMarkAllAsRead(String userId) {
        List<Notification> list = notificationMapper.findByRecipient(userId, LIST_LIMIT);
        if (!list.isEmpty()) {
            // 一覧は新しい順なので、先頭が一覧を開いた時点での最新通知
            notificationMapper.markAsReadUpTo(userId, list.get(0).getNotificationId());
        }
        return list;
    }

    /** 未読件数(ヘッダーのバッジ用) */
    public int countUnread(String userId) {
        return notificationMapper.countUnread(userId);
    }

    // ============================================
    // 内部処理
    // ============================================

    /** 共通ルールを適用して、通知を1件登録する */
    private void createNotification(NotificationType type, String recipientId, String senderId, String postId) {
        // 投稿系の通知は postId が必須、それ以外は postId を持たない(呼び出しの書き間違いを防ぐ)
        if (type.isPostRelated() != (postId != null)) {
            throw new IllegalArgumentException(type + " の通知では postId は" + (type.isPostRelated() ? "必須" : "不要") + "です。");
        }

        // 自分自身には通知しない
        if (recipientId.equals(senderId)) {
            return;
        }

        // 同じ内容の未読通知がすでにあれば、重複して作らない
        int sameUnread = (postId == null)
                ? notificationMapper.countSameUnreadWithoutPost(recipientId, senderId, type.getCode())
                : notificationMapper.countSameUnreadWithPost(recipientId, senderId, type.getCode(), postId);
        if (sameUnread > 0) {
            return;
        }

        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setSenderId(senderId);
        notification.setNotificationType(type.getCode());
        notification.setPostId(postId);
        notificationMapper.insert(notification);

        // 通知を登録したことを知らせる(プッシュ通知を送る処理などが受け取る)。
        // 実際に受け取る処理は、この処理のトランザクションがコミットされた後に実行される
        eventPublisher.publishEvent(new NotificationCreatedEvent(recipientId, senderId, type.getCode(), postId));
    }
}