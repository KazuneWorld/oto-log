// 設定画面での、プッシュ通知のオン・オフ。
//
// 必要なHTML: settings.html(#push-section、#push-status、2つのボタン)と、
// <head> の CSRFトークンの meta タグ(_csrf / _csrf_header)。

(function () {
    var section = document.getElementById("push-section");
    var statusEl = document.getElementById("push-status");
    var enableButton = document.getElementById("push-enable-button");
    var disableButton = document.getElementById("push-disable-button");

    if (!section || !statusEl || !enableButton || !disableButton) {
        return; // このページには、通知の設定がない
    }

    var API_PUBLIC_KEY = "/api/push/public-key";
    var API_SUBSCRIBE = "/api/push/subscribe";
    var API_UNSUBSCRIBE = "/api/push/unsubscribe";

    // ---------- 共通 ----------

    function showStatus(message) {
        statusEl.textContent = message;
    }

    // 画面に出す状態: "on"(オン) / "off"(オフ) / "unavailable"(使えない)
    function render(state, message) {
        section.hidden = false;
        enableButton.hidden = state !== "off";
        disableButton.hidden = state !== "on";
        showStatus(message);
    }

    // Spring SecurityのCSRFトークンを、metaタグからヘッダーにして返す
    function csrfHeaders() {
        var token = document.querySelector('meta[name="_csrf"]');
        var header = document.querySelector('meta[name="_csrf_header"]');
        var headers = { "Content-Type": "application/json" };
        if (token && header && token.content && header.content) {
            headers[header.content] = token.content;
        }
        return headers;
    }

    function post(url, body) {
        return fetch(url, { method: "POST", headers: csrfHeaders(), body: JSON.stringify(body) });
    }

    // base64url文字列 → Uint8Array(購読の時に、VAPID公開鍵をこの形式で渡す必要がある)
    function urlBase64ToUint8Array(base64String) {
        var padding = "=".repeat((4 - (base64String.length % 4)) % 4);
        var base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
        var raw = atob(base64);
        var output = new Uint8Array(raw.length);
        for (var i = 0; i < raw.length; i++) {
            output[i] = raw.charCodeAt(i);
        }
        return output;
    }

    function sameKey(buffer, uint8Array) {
        var a = new Uint8Array(buffer);
        if (a.length !== uint8Array.length) {
            return false;
        }
        for (var i = 0; i < a.length; i++) {
            if (a[i] !== uint8Array[i]) {
                return false;
            }
        }
        return true;
    }

    // ---------- 動作環境のチェック ----------

    var isIOS = /iPhone|iPad|iPod/.test(navigator.userAgent)
        || (navigator.platform === "MacIntel" && navigator.maxTouchPoints > 1);
    var isStandalone = window.matchMedia("(display-mode: standalone)").matches
        || navigator.standalone === true;

    // 使えない理由を返す。使えるなら null
    function unavailableReason() {
        if (!window.isSecureContext) {
            return "プッシュ通知は、HTTPSのアドレスで開いた時だけ使えます。";
        }
        // iPhoneは、ホーム画面に追加したアプリからでないと、プッシュ通知を使えない(iOS 16.4以降)
        if (isIOS && !isStandalone) {
            return "iPhoneでは、Safariの共有メニューから「ホーム画面に追加」して、ホーム画面のアイコンから開くと、プッシュ通知を使えます。";
        }
        if (!("serviceWorker" in navigator) || !("PushManager" in window) || !("Notification" in window)) {
            return "このブラウザは、プッシュ通知に対応していません。";
        }
        return null;
    }

    // ---------- 状態の表示 ----------

    var registration = null;
    var publicKey = null;

    function refresh() {
        return registration.pushManager.getSubscription().then(function (subscription) {
            if (Notification.permission === "denied") {
                render("unavailable", "通知がブロックされています。ブラウザ(または端末)の設定で、このサイトの通知を許可してください。");
                return;
            }
            if (subscription) {
                // すでにこの端末で購読している。サーバー側の記録が消えていても(開発中のDBの初期化など)
                // 通知が届くよう、サーバーに登録し直しておく
                return post(API_SUBSCRIBE, subscription.toJSON()).then(function () {
                    render("on", "この端末でプッシュ通知を受け取る設定になっています。");
                }).catch(function () {
                    render("on", "この端末でプッシュ通知を受け取る設定になっています。");
                });
            }
            render("off", "現在この端末では、プッシュ通知はオフです。");
        });
    }

    // ---------- オンにする ----------

    enableButton.addEventListener("click", function () {
        enableButton.disabled = true;

        Notification.requestPermission().then(function (permission) {
            if (permission !== "granted") {
                throw new Error("通知が許可されませんでした。");
            }
            return registration.pushManager.getSubscription();
        }).then(function (existing) {
            var appKey = urlBase64ToUint8Array(publicKey);
            // 以前のVAPIDキーで作られた購読は、作り直す
            if (existing && existing.options && existing.options.applicationServerKey
                && !sameKey(existing.options.applicationServerKey, appKey)) {
                return existing.unsubscribe().then(function () { return null; });
            }
            return existing;
        }).then(function (existing) {
            return existing || registration.pushManager.subscribe({
                userVisibleOnly: true, // 「受信したら必ず通知を表示する」という約束(必須)
                applicationServerKey: urlBase64ToUint8Array(publicKey)
            });
        }).then(function (subscription) {
            return post(API_SUBSCRIBE, subscription.toJSON()).then(function (response) {
                if (!response.ok) {
                    return response.json().then(function (body) {
                        // サーバーに登録できなかった場合は、ブラウザ側の購読も取り消して、状態をそろえる
                        return subscription.unsubscribe().then(function () {
                            throw new Error(body.message || "登録に失敗しました。");
                        });
                    });
                }
            });
        }).then(refresh).catch(function (error) {
            showStatus(error.message || "プッシュ通知をオンにできませんでした。");
        }).then(function () {
            enableButton.disabled = false;
        });
    });

    // ---------- オフにする ----------

    disableButton.addEventListener("click", function () {
        disableButton.disabled = true;

        registration.pushManager.getSubscription().then(function (subscription) {
            if (!subscription) {
                return null;
            }
            var endpoint = subscription.endpoint;
            // 先にサーバーの記録を消してから、ブラウザ側の購読を解除する
            return post(API_UNSUBSCRIBE, { endpoint: endpoint }).then(function () {
                return subscription.unsubscribe();
            });
        }).then(refresh).catch(function () {
            showStatus("プッシュ通知をオフにできませんでした。通信状況を確認して、もう一度お試しください。");
        }).then(function () {
            disableButton.disabled = false;
        });
    });

    // ---------- 起動時の処理 ----------

    var reason = unavailableReason();
    if (reason) {
        render("unavailable", reason);
        return;
    }

    // サーバー側でプッシュ通知が無効(VAPIDキー未設定)の場合は、404が返る → 設定自体を出さない
    fetch(API_PUBLIC_KEY).then(function (response) {
        if (!response.ok) {
            return null;
        }
        return response.text();
    }).then(function (key) {
        if (!key) {
            return;
        }
        publicKey = key.trim();
        return navigator.serviceWorker.ready.then(function (reg) {
            registration = reg;
            return refresh();
        });
    }).catch(function () {
        // 通信に失敗した場合は、設定を出さない(通知はおまけの機能なので、エラーも出さない)
    });
})();
