// おとログ Service Worker
//
// 役割は「オフライン時に案内ページ(offline.html)を表示すること」だけに絞っている。
//
// このアプリはログインが必要で、画面の中身(タイムライン・通知など)がユーザーごとに
// 変わり、フォームにはCSRFトークンも含まれる。そのため、HTMLやAPIのレスポンスを
// キャッシュして使い回すと「古い内容・別の人の内容・期限切れのトークン」を表示する
// 危険がある。CSS/JSもキャッシュしない(開発中に「修正したのに反映されない」を避けるため)。
// オフライン対応を広げたくなったら、ここに戦略を足していく。

// キャッシュの名前。offline.html の内容を変えた時は、ここのバージョンを上げる
const CACHE_NAME = "otolog-v1";
const OFFLINE_URL = "/offline.html";

// インストール時に、オフライン用ページだけをキャッシュしておく
self.addEventListener("install", function (event) {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(function (cache) {
                return cache.addAll([OFFLINE_URL]);
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
