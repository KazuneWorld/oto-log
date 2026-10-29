package com.example.webapp.push;

import com.example.webapp.entity.PushSubscription;

/**
 * プッシュ通知を実際に送る部品の「窓口」。
 * 本番は WebPushSender(web-pushライブラリ)が実装している。
 * 送信処理をインターフェースにしておくことで、テストでは本物の送信をせずに差し替えられる。
 */
public interface PushGateway {

    /** プッシュ通知が使える状態か(VAPIDキーが設定されているか) */
    boolean isEnabled();

    /** VAPID公開鍵(ブラウザが購読する時に必要)。使えない状態なら null */
    String getPublicKey();

    /**
     * 1つの購読先に、通知を1件送る。
     *
     * @param payloadJson 通知の内容(JSON文字列)
     * @return プッシュサービスが返したHTTPステータス(201 = 受け付けた、404/410 = その宛先はもう無効)
     */
    int send(PushSubscription subscription, String payloadJson) throws Exception;
}
