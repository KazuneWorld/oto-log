package com.example.webapp.service;

/**
 * 通知が登録された(notificationsテーブルに1件追加された)ことを知らせるイベント。
 *
 * NotificationService が通知を登録するたびに発行する。
 * 通知を作る側(NotificationService)は、このイベントを誰が受け取るかを知らなくてよい。
 * 現在は、プッシュ通知を送る処理(PushNotificationListener)が受け取っている。
 */
public record NotificationCreatedEvent(String recipientId, String senderId, int notificationType, String postId) {
}
