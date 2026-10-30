package com.example.webapp.push;

import com.example.webapp.entity.PushSubscription;
import nl.martijndwars.webpush.Encoding;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Urgency;
import nl.martijndwars.webpush.Utils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * web-pushライブラリを使って、実際にプッシュ通知を送る。
 *
 * VAPIDキー(application.properties の push.vapid.*)が未設定の場合は「無効」状態で起動し、
 * プッシュ通知は一切送らない(ローカル開発で、キーなしでもアプリが動くようにするため)。
 * キーが設定されているのに形式が不正・公開鍵と秘密鍵が対応していない場合は、
 * 気づかず動かない状態になるのを防ぐため、起動時にエラーにする。
 */
@Component
public class WebPushSender implements PushGateway {

    private static final Logger log = LoggerFactory.getLogger(WebPushSender.class);

    // 端末がオフラインなどで届けられない時に、プッシュサービスが通知を保管しておく時間(秒)
    private static final int TTL_SECONDS = 24 * 60 * 60;

    private final String publicKey;       // 無効状態なら null
    private final PushService pushService; // 無効状態なら null

    public WebPushSender(@Value("${push.vapid.public-key:}") String publicKey,
                         @Value("${push.vapid.private-key:}") String privateKey,
                         @Value("${push.vapid.subject:mailto:admin@example.com}") String subject)
            throws GeneralSecurityException {

        if (publicKey == null || publicKey.isBlank() || privateKey == null || privateKey.isBlank()) {
            this.publicKey = null;
            this.pushService = null;
            log.info("プッシュ通知は無効です(push.vapid.public-key / private-key が未設定)。");
            return;
        }

        // 暗号化処理にBouncyCastleを使うため、プロバイダーを登録しておく
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        String pub = publicKey.trim();
        String priv = privateKey.trim();
        if (!Utils.verifyKeyPair(Utils.loadPrivateKey(priv), Utils.loadPublicKey(pub))) {
            throw new IllegalArgumentException("push.vapid.public-key と push.vapid.private-key が対応していません。"
                    + "同じ組み合わせ(VapidKeyGeneratorで一緒に作ったもの)を設定してください。");
        }

        this.publicKey = pub;
        this.pushService = new PushService(pub, priv, subject);
        log.info("プッシュ通知は有効です。");
    }

    @Override
    public boolean isEnabled() {
        return pushService != null;
    }

    @Override
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    public int send(PushSubscription subscription, String payloadJson) throws Exception {
        if (pushService == null) {
            throw new IllegalStateException("プッシュ通知が無効です。");
        }

        Notification notification = Notification.builder()
                .endpoint(subscription.getEndpoint())
                .userPublicKey(subscription.getP256dh())
                .userAuth(subscription.getAuth())
                .payload(payloadJson)
                .ttl(TTL_SECONDS)
                .urgency(Urgency.HIGH) // 端末が待機中でも、できるだけすぐ届けるよう頼む
                .build();

        // 【重要】暗号化方式は AES128GCM(現在の標準)を明示する。
        // send(notification) のように指定しないと、古い方式(AESGCM)で送ってしまい、
        // iPhone(Safari)などで受け取れないことがある。
        HttpResponse response = pushService.send(notification, Encoding.AES128GCM);
        return response.getStatusLine().getStatusCode();
    }
}
