package com.example.webapp.mapper;

import com.example.webapp.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 通知のMapper。SQLは NotificationMapper.xml にある。
 *
 * ここのメソッドは通知の「部品」。通知を作りたい時は、これを直接呼ばず
 * NotificationService の notify〇〇〇() を使うこと
 * (自分宛ての除外や、重複の防止などのルールは Service 側にある)。
 */
@Mapper
public interface NotificationMapper {

    /** 通知を1件登録する(既読=false、作成日時=現在時刻) */
    void insert(Notification notification);

    /** 同じ内容の未読通知の件数(投稿に紐づかない通知用)。1件以上あれば重複 */
    int countSameUnreadWithoutPost(@Param("recipientId") String recipientId,
                                   @Param("senderId") String senderId,
                                   @Param("notificationType") int notificationType);

    /** 同じ内容の未読通知の件数(投稿に紐づく通知用)。1件以上あれば重複 */
    int countSameUnreadWithPost(@Param("recipientId") String recipientId,
                                @Param("senderId") String senderId,
                                @Param("notificationType") int notificationType,
                                @Param("postId") String postId);

    /** 投稿の持ち主のuser_idを取得する(存在しない投稿なら null) */
    String findPostOwnerId(@Param("postId") String postId);

    /**
     * 「この人の投稿を通知してほしい」と設定しているフレンドのuser_idの一覧。
     * friends の requester_notify_on_post / receiver_notify_on_post を、関係の向きに合わせて見る。
     */
    List<String> findNotifyingFriendIds(@Param("posterId") String posterId);

    /** 通知の一覧(新しい順)。送り主のニックネーム・アイコン付き */
    List<Notification> findByRecipient(@Param("recipientId") String recipientId,
                                       @Param("limit") int limit);

    /** 未読の件数 */
    int countUnread(@Param("recipientId") String recipientId);

    /** 指定したIDまで(一覧を開いた時点まで)の未読通知を、既読にする */
    int markAsReadUpTo(@Param("recipientId") String recipientId,
                       @Param("maxNotificationId") int maxNotificationId);
}