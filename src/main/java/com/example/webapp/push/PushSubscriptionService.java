package com.example.webapp.push;

import com.example.webapp.entity.PushSubscription;
import com.example.webapp.mapper.PushSubscriptionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

/**
 * プッシュ通知の購読情報の登録・解除。
 */
@Service
@Transactional
public class PushSubscriptionService {

    /** 1人のユーザーが登録できる購読情報(端末)の最大数 */
    public static final int MAX_PER_USER = 10;

    /** 登録の結果 */
    public enum Result {
        OK,
        /** 送り先や鍵の形式が正しくない */
        INVALID,
        /** 登録できる端末の数の上限を超えている */
        TOO_MANY
    }

    private final PushSubscriptionMapper pushSubscriptionMapper;

    public PushSubscriptionService(PushSubscriptionMapper pushSubscriptionMapper) {
        this.pushSubscriptionMapper = pushSubscriptionMapper;
    }

    /**
     * 購読情報を登録する。
     * 同じendpoint(=同じ端末・ブラウザ)がすでに登録されている場合は、登録し直す。
     * (同じ端末で別のユーザーがログインし直した場合に、通知が前のユーザーに届き続けないようにするため)
     */
    public Result register(String userId, String endpoint, String p256dh, String auth) {
        if (userId == null || !PushEndpointValidator.isAllowed(endpoint)
                || !isValidP256dh(p256dh) || !isValidAuth(auth)) {
            return Result.INVALID;
        }

        PushSubscription existing = pushSubscriptionMapper.findByEndpoint(endpoint);
        boolean belongsToUser = existing != null && userId.equals(existing.getUserId());

        // 他ユーザーの購読を削除する前に上限を確認する。
        // 上限超過時に、既存ユーザーの購読だけが消えることを防ぐ。
        if (!belongsToUser && pushSubscriptionMapper.countByUserId(userId) >= MAX_PER_USER) {
            return Result.TOO_MANY;
        }

        if (existing != null) {
            pushSubscriptionMapper.deleteByEndpoint(endpoint);
        }

        PushSubscription subscription = new PushSubscription();
        subscription.setUserId(userId);
        subscription.setEndpoint(endpoint);
        subscription.setP256dh(p256dh);
        subscription.setAuth(auth);
        pushSubscriptionMapper.insert(subscription);
        return Result.OK;
    }

    /** 自分の購読情報を解除する(他のユーザーのものは消せない) */
    public void unregister(String userId, String endpoint) {
        if (userId == null || endpoint == null) {
            return;
        }
        pushSubscriptionMapper.deleteByEndpointAndUserId(endpoint, userId);
    }

    // 端末の公開鍵(p256dh)は、base64urlで、65バイト(先頭が0x04の非圧縮形式)
    private static boolean isValidP256dh(String p256dh) {
        byte[] bytes = decode(p256dh);
        return bytes != null && bytes.length == 65 && bytes[0] == 0x04;
    }

    // 認証用の秘密(auth)は、base64urlで、16バイト
    private static boolean isValidAuth(String auth) {
        byte[] bytes = decode(auth);
        return bytes != null && bytes.length == 16;
    }

    private static byte[] decode(String base64Url) {
        if (base64Url == null || base64Url.isEmpty() || base64Url.length() > 100) {
            return null;
        }
        try {
            return Base64.getUrlDecoder().decode(base64Url);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
