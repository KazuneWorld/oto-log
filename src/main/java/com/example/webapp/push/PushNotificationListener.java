package com.example.webapp.push;

import com.example.webapp.service.NotificationCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 通知が登録されたら、プッシュ通知を送る。
 *
 *  ・@TransactionalEventListener(AFTER_COMMIT)
 *      通知の登録がコミットされた「後」に実行する。登録が取り消された(ロールバックされた)のに、
 *      プッシュ通知だけ送られてしまう、ということを防ぐ。
 *      fallbackExecution = true は、トランザクションがない場所から通知が作られた場合でも実行するための指定。
 *  ・@Async
 *      別のスレッドで実行する。プッシュサービスへの通信に時間がかかっても、
 *      フレンド申請などの画面の応答が遅くならない。
 */
@Component
public class PushNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationListener.class);

    private final PushNotificationService pushNotificationService;

    public PushNotificationListener(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        try {
            pushNotificationService.send(event);
        } catch (Exception e) {
            log.warn("プッシュ通知の処理中に予期しないエラーが発生しました。", e);
        }
    }
}
