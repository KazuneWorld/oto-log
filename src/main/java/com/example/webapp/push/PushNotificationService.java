package com.example.webapp.push;

import com.example.webapp.entity.NotificationType;
import com.example.webapp.entity.NotificationContent;
import com.example.webapp.entity.NotificationSender;
import com.example.webapp.entity.PushSubscription;
import com.example.webapp.mapper.NotificationMapper;
import com.example.webapp.mapper.PushSubscriptionMapper;
import com.example.webapp.service.NotificationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 登録された通知を、受信者の全ての端末にプッシュ通知として送る。
 *
 * 送信が失敗しても、例外は外に出さない(ログに残すだけ)。
 * プッシュ通知は「おまけ」の機能で、失敗したからといって、
 * フレンド申請やリアクションなどの本来の処理に影響を与えてはいけないため。
 */
@Service
public class PushNotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

    private static final String TITLE = "おとログ";

    private final PushGateway pushGateway;
    private final PushSubscriptionMapper pushSubscriptionMapper;
    private final NotificationMapper notificationMapper;

    public PushNotificationService(PushGateway pushGateway,
                                   PushSubscriptionMapper pushSubscriptionMapper,
                                   NotificationMapper notificationMapper) {
        this.pushGateway = pushGateway;
        this.pushSubscriptionMapper = pushSubscriptionMapper;
        this.notificationMapper = notificationMapper;
    }

    public void send(NotificationCreatedEvent event) {
        if (!pushGateway.isEnabled()) {
            return;
        }

        // 通知を送る対象の端末一覧を取得
        List<PushSubscription> subscriptions = pushSubscriptionMapper.findByUserId(event.recipientId());
        if (subscriptions.isEmpty()) {
            return; // 通知をオンにしている端末がない
        }

        // 通知の文言とリンク先は、アプリ内の通知一覧と同じ(NotificationType)
        NotificationType type = NotificationType.fromCode(event.notificationType());
        NotificationSender sender = notificationMapper.findSenderByUserId(event.senderId());
        String nickname = sender == null ? null : sender.getNickname();
        NotificationContent content = event.postId() == null
                ? null
                : notificationMapper.findNotificationContent(event.postId(), event.senderId());
        String trackName = content == null ? null : content.getTrackName();
        String reactionEmoji = content == null || type != NotificationType.REACTION
                ? null
                : content.getReactionEmoji();
        String body = type.message(nickname != null ? nickname : "だれか", trackName, reactionEmoji);
        String url = type.linkUrl(event.senderId(), event.postId());
        String icon = sender == null || sender.getIcon() == null || sender.getIcon().isBlank()
                ? null
                : "/images/usericons/" + sender.getIcon();
        String payload = PushPayload.toJson(TITLE, body, url, icon);

        for (PushSubscription subscription : subscriptions) {
            sendTo(subscription, payload);
        }
    }

    private void sendTo(PushSubscription subscription, String payload) {
        try {
            int status = pushGateway.send(subscription, payload);

            if (status == 404 || status == 410) {
                // その宛先はもう使えない(ブラウザ側で購読が解除された、アプリが削除された等)ので、削除する
                pushSubscriptionMapper.deleteByEndpoint(subscription.getEndpoint());
                log.info("無効な購読を削除しました。ユーザー={}, ステータス={}", subscription.getUserId(), status);
            } else if (status < 200 || status >= 300) {
                // 401/403 = VAPIDキーの問題、429 = 送りすぎ、5xx = プッシュサービス側の障害 など
                log.warn("プッシュ通知の送信に失敗しました。ユーザー={}, ステータス={}", subscription.getUserId(), status);
            }
        } catch (Exception e) {
            log.warn("プッシュ通知の送信中にエラーが発生しました。ユーザー={}", subscription.getUserId(), e);
        }
    }
}
