package com.example.webapp.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知(notificationsテーブル1行分)。
 * 一覧画面で表示するため、送り主(sender)のニックネームとアイコンも一緒に持つ。
 */
@Data
public class Notification {

    private Integer notificationId;
    private String recipientId;     // 通知を受け取る人
    private String senderId;        // 通知の発生元の人
    private int notificationType;   // NotificationType の code（0: フレンド申請、1: 承認、2: リアクション、3: フレンドの投稿）
    private String postId;          // 投稿系の通知のみ。それ以外は null
    private boolean read;           // 既読か？(DBの is_read)
    private LocalDateTime createdAt;

    // 以下は一覧表示用(usersテーブルをJOINして取得する)
    private String senderNickname;
    private String senderIcon;

    // ---- テンプレート(Thymeleaf)から ${n.message} ${n.linkUrl} で使えるようにしている ----

    /** 通知の文言(例: 「〇〇さんからフレンド申請が届きました」) */
    public String getMessage() {
        return NotificationType.fromCode(notificationType).message(senderNickname);
    }

    /** 通知をタップした時の遷移先URL */
    public String getLinkUrl() {
        return NotificationType.fromCode(notificationType).linkUrl(senderId, postId);
    }
}