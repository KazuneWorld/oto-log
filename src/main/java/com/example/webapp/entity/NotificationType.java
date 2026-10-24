package com.example.webapp.entity;

/**
 * 通知の種類。
 *
 * notifications.notification_type には、このenumの code(番号)を保存する。
 * 番号・通知の文言・リンク先をここにまとめているので、他の場所で番号を直書きしないこと。
 * 将来PWAのプッシュ通知を送る際の本文にも、message() をそのまま使える。
 */
public enum NotificationType {

    /** フレンド申請が届いた(リンク先: 承認画面) */
    FRIEND_REQUEST(0, "さんからフレンド申請が届きました"),

    /** 自分が送った申請が承認され、フレンドになった(リンク先: 相手のプロフィール) */
    FRIEND_ACCEPTED(1, "さんとフレンドになりました"),

    /** 自分の投稿にリアクションが付いた(リンク先: その投稿) */
    REACTION(2, "さんがあなたの投稿にリアクションしました"),

    /** 通知ONにしているフレンドが投稿した(リンク先: その投稿) */
    FRIEND_POSTED(3, "さんが新しい曲を投稿しました");

    private final int code;
    private final String messageSuffix;

    NotificationType(int code, String messageSuffix) {
        this.code = code;
        this.messageSuffix = messageSuffix;
    }

    public int getCode() {
        return code;
    }

    /** 通知の文言(例: 「〇〇さんからフレンド申請が届きました」) */
    public String message(String senderNickname) {
        return senderNickname + messageSuffix;
    }

    /** 投稿に紐づく通知か(true の場合、notifications.post_id が必須になる) */
    public boolean isPostRelated() {
        return this == REACTION || this == FRIEND_POSTED;
    }

    /** 通知をタップした時の遷移先URL */
    public String linkUrl(String senderId, String postId) {
        switch (this) {
            case FRIEND_REQUEST:
                return "/friend/requests";
            case FRIEND_ACCEPTED:
                return "/" + senderId;
            default:
                return "/post/" + postId;
        }
    }

    /** DBに保存された番号から、種類を取得する */
    public static NotificationType fromCode(int code) {
        for (NotificationType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未定義の通知の種類です: " + code);
    }
}