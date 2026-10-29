package com.example.webapp.api;

import com.example.webapp.push.PushGateway;
import com.example.webapp.push.PushSubscriptionService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * プッシュ通知の購読を管理するAPI(設定画面の push-settings.js から呼ばれる)。
 * 全て、ログインしている人だけが使える想定(SecurityConfigで /api/** は認証必須にしておく)。
 */
@RestController
@RequestMapping("/api/push")
public class PushApiController {

    private final PushGateway pushGateway;
    private final PushSubscriptionService pushSubscriptionService;

    public PushApiController(PushGateway pushGateway,
                             PushSubscriptionService pushSubscriptionService) {
        this.pushGateway = pushGateway;
        this.pushSubscriptionService = pushSubscriptionService;
    }

    /** VAPID公開鍵。プッシュ通知が無効な設定なら 404(画面側は、通知の設定を非表示にする) */
    @GetMapping("/public-key")
    public ResponseEntity<String> publicKey() {
        if (!pushGateway.isEnabled()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pushGateway.getPublicKey());
    }

    /** 購読情報を登録する(通知をオンにした時) */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestBody SubscribeRequest request, Authentication authentication) {
        String userId = authentication.getName();
        if (userId == null) {
            return message(HttpStatus.UNAUTHORIZED, "ログインが必要です。");
        }
        if (!pushGateway.isEnabled()) {
            return message(HttpStatus.NOT_FOUND, "プッシュ通知は現在利用できません。");
        }
        if (request == null || request.getKeys() == null) {
            return message(HttpStatus.BAD_REQUEST, "購読情報が正しくありません。");
        }

        PushSubscriptionService.Result result = pushSubscriptionService.register(
                userId, request.getEndpoint(), request.getKeys().getP256dh(), request.getKeys().getAuth());

        switch (result) {
            case OK:
                return message(HttpStatus.CREATED, "登録しました。");
            case TOO_MANY:
                return message(HttpStatus.CONFLICT, "通知をオンにできる端末の数の上限に達しています。使っていない端末で、通知をオフにしてください。");
            default:
                return message(HttpStatus.BAD_REQUEST, "購読情報が正しくありません。");
        }
    }

    /** 購読情報を解除する(通知をオフにした時)。自分の購読情報だけが対象 */
    @PostMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestBody UnsubscribeRequest request, Authentication authentication) {
        String userId = authentication.getName();
        if (userId == null) {
            return message(HttpStatus.UNAUTHORIZED, "ログインが必要です。");
        }
        if (request != null) {
            pushSubscriptionService.unregister(userId, request.getEndpoint());
        }
        return message(HttpStatus.OK, "解除しました。");
    }

    private static ResponseEntity<Map<String, String>> message(HttpStatus status, String text) {
        return ResponseEntity.status(status).body(Map.of("message", text));
    }

    /** ブラウザの PushSubscription を JSON にしたもの { endpoint, expirationTime, keys: { p256dh, auth } } */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscribeRequest {
        private String endpoint;
        private Keys keys;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Keys {
            private String p256dh;
            private String auth;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UnsubscribeRequest {
        private String endpoint;
    }
}
