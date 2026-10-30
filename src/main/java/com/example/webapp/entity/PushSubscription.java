package com.example.webapp.entity;

import lombok.Data;

/**
 * プッシュ通知の購読情報(push_subscriptionsテーブル1行分)。
 * 1人のユーザーが、複数の端末(スマホ・PCなど)で購読できるので、1ユーザーに複数行ある。
 */
@Data
public class PushSubscription {

    private Integer pushSubscriptionId;
    private String userId;      // 通知を受け取るユーザー
    private String endpoint;    // 通知の送り先URL(Google/Apple/Mozillaなどのプッシュサービスが端末ごとに発行する)
    private String p256dh;      // 端末の公開鍵(通知の暗号化に使う)
    private String auth;        // 端末の認証用の秘密(通知の暗号化に使う)
}
