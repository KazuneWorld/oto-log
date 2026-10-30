package com.example.webapp.push;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 購読情報の送り先(endpoint)が、実在するプッシュサービスのものかを確認する。
 *
 * endpointはブラウザから送られてくる値で、サーバーはその宛先に向けてHTTPリクエストを送る。
 * 何も確認しないと、悪意のあるユーザーが任意のURL(社内ネットワークなど)を登録して、
 * サーバーにリクエストを送らせることができてしまう(SSRFという脆弱性)。
 * そのため、既知のプッシュサービスのホストだけを許可する。
 */
public final class PushEndpointValidator {

    /** endpointとして許す最大の長さ(DBのカラムの長さと合わせる) */
    public static final int MAX_LENGTH = 500;

    private static final Pattern ALLOWED_HOST = Pattern.compile(
            "^(" +
            "fcm\\.googleapis\\.com" +                          // Chrome / Edge / Android(FCM)
            "|android\\.googleapis\\.com" +                     // 古いChrome(GCM)
            "|([a-z0-9-]+\\.)*push\\.services\\.mozilla\\.com" + // Firefox
            "|([a-z0-9-]+\\.)*push\\.apple\\.com" +             // Safari / iPhone
            "|([a-z0-9-]+\\.)*notify\\.windows\\.com" +         // Windows(WNS)
            ")$");

    private PushEndpointValidator() {
    }

    public static boolean isAllowed(String endpoint) {
        if (endpoint == null || endpoint.isEmpty() || endpoint.length() > MAX_LENGTH) {
            return false;
        }
        try {
            URI uri = new URI(endpoint);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                return false;
            }
            if (uri.getUserInfo() != null) {
                return false; // https://許可ホスト@evil.example/ のような、ホストの偽装を防ぐ
            }
            if (uri.getPort() != -1 && uri.getPort() != 443) {
                return false;
            }
            String host = uri.getHost();
            return host != null && ALLOWED_HOST.matcher(host.toLowerCase(Locale.ROOT)).matches();
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
