// おとログ Service Worker
//
// 役割は次の2つ。
//   1. オフライン時に案内ページ(offline.html)を表示する
//   2. サーバーからのプッシュ通知を受け取って、通知を表示する(ファイルの末尾)
//
// 1 については、次の方針で、キャッシュを最小限に絞っている。
//
// このアプリはログインが必要で、画面の中身(タイムライン・通知など)がユーザーごとに
// 変わり、フォームにはCSRFトークンも含まれる。そのため、HTMLやAPIのレスポンスを
// キャッシュして使い回すと「古い内容・別の人の内容・期限切れのトークン」を表示する
// 危険がある。CSS/JSもキャッシュしない(開発中に「修正したのに反映されない」を避けるため)。
// オフライン対応を広げたくなったら、ここに戦略を足していく。

// キャッシュの名前。offline.html の内容や、このファイルの取得ロジックを変えた時は、ここのバージョンを上げる
const CACHE_NAME = "otolog-v4";
const OFFLINE_URL = "/offline.html";

// インストール時に、オフライン用ページをキャッシュしておく
//
// 取得したものが「本物のoffline.html」でない場合は、黙ってキャッシュせず、
// わざとインストールを失敗させてコンソールにエラーを出す。
// 特に、Spring Securityの設定で /offline.html が認証必須になっていると、
// ログイン画面へのリダイレクト結果が「offline.html」としてキャッシュされてしまい、
// オフライン時に真っ白な画面やエラーになる(リダイレクトされた応答は、
// ページ遷移の応答としては使えないため)。
self.addEventListener("install", function (event) {
    event.waitUntil(
        fetch(OFFLINE_URL, { cache: "reload" })
            .then(function (response) {
                const contentType = response.headers.get("Content-Type") || "";

                if (!response.ok || response.redirected || contentType.indexOf("text/html") === -1) {
                    throw new Error(
                        "offline.html を正しく取得できませんでした(status=" + response.status +
                        ", redirected=" + response.redirected + ")。" +
                        "/offline.html が認証なしで開けるか(Spring Securityのpermit設定)を確認してください。"
                    );
                }

                return caches.open(CACHE_NAME).then(function (cache) {
                    return cache.put(OFFLINE_URL, response);
                });
            })
            .then(function () {
                // 新しいService Workerを、待たずにすぐ有効化する
                return self.skipWaiting();
            })
    );
});

// 有効化時に、古いバージョンのキャッシュを削除する
self.addEventListener("activate", function (event) {
    event.waitUntil(
        caches.keys()
            .then(function (keys) {
                return Promise.all(
                    keys
                        .filter(function (key) { return key !== CACHE_NAME; })
                        .map(function (key) { return caches.delete(key); })
                );
            })
            .then(function () {
                // 開いているページも、すぐこのService Workerの管理下にする
                return self.clients.claim();
            })
    );
});

// ページ遷移(GETのナビゲーション)だけを横取りし、
// ネットワークに繋がらなかった時だけ offline.html を返す。
// それ以外(API・画像・CSS/JS・POSTなど)には何もせず、ブラウザの通常の動作に任せる。
self.addEventListener("fetch", function (event) {
    const request = event.request;

    if (request.method !== "GET" || request.mode !== "navigate") {
        return;
    }

    event.respondWith(
        fetch(request).catch(function () {
            return caches.match(OFFLINE_URL).then(function (cached) {
                // 万一キャッシュにも無かった場合の最低限の応答
                return cached || new Response("オフラインです。ネットワークに接続してから、もう一度お試しください。", {
                    status: 503,
                    headers: { "Content-Type": "text/plain; charset=utf-8" }
                });
            });
        })
    );
});


// ============================================
// プッシュ通知
// ============================================

// 通知から開く画面は、このサイト内のパス(例: /post/xxxx)だけに限る。
// "//evil.example" のような、別のサイトに飛ばされる値が来ても、トップに戻す(念のための安全対策)
function safeUrl(url) {
    return (typeof url === "string" && /^\/(?!\/)/.test(url)) ? url : "/";
}

// サーバーからプッシュを受信した時(アプリを閉じていても呼ばれる)。
// 本文は PushPayload.toJson が作る {title, body, url, icon, image} のJSON
self.addEventListener("push", function (event) {
    let data = {};
    try {
        data = event.data ? event.data.json() : {};
    } catch (e) {
        // JSONでない内容が届いた場合は、文字列をそのまま本文にする
        data = { body: event.data ? event.data.text() : "" };
    }

    // 受信したら、必ず通知を表示する。
    // (iPhoneなどは、プッシュを受信して通知を出さないと、購読を取り消されることがある)
    const notificationOptions = {
        body: data.body || "新しい通知があります",
        icon: data.icon || "/images/pwa/icon-192.png",
        lang: "ja",
        data: { url: safeUrl(data.url) }
    };
    if (typeof data.image === "string" && data.image.trim()) {
        notificationOptions.image = data.image;
    }

    event.waitUntil(
        self.registration.showNotification(data.title || "おとログ", notificationOptions)
    );
});

// 通知をタップした時: 開いているアプリがあれば、通知の対象の画面に移動して前面に出す。なければ新しく開く
self.addEventListener("notificationclick", function (event) {
    event.notification.close();
    const target = new URL(safeUrl(event.notification.data && event.notification.data.url), self.location.origin).href;

    event.waitUntil(
        self.clients.matchAll({ type: "window", includeUncontrolled: true }).then(function (windows) {
            for (const client of windows) {
                if ("focus" in client) {
                    if ("navigate" in client) {
                        return client.navigate(target)
                            .then(function (navigated) { return (navigated || client).focus(); })
                            .catch(function () { return client.focus(); });
                    }
                    return client.focus();
                }
            }
            return self.clients.openWindow(target);
        })
    );
});