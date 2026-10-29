package com.example.webapp.push;

/**
 * プッシュ通知の本文(JSON)を組み立てる。
 *
 * ニックネームなど、ユーザーが自由に入力した文字が入るので、
 * 引用符・改行・制御文字を正しくエスケープして、必ず正しいJSONにする。
 * (ここで組み立てた内容は、Service Worker(sw.js)の push イベントで読み取られる)
 */
public final class PushPayload {

    private PushPayload() {
    }

    /**
     * @param title 通知のタイトル
     * @param body  通知の本文
     * @param url   通知をタップした時に開く、このサイト内のパス(例: /post/xxxx)
     * @param icon  通知に表示するアイコンURL。nullの場合はService Worker側の既定アイコンを使う
     * @param image 通知に表示する画像URL。nullの場合は画像を表示しない
     */
    public static String toJson(String title, String body, String url, String icon, String image) {
        return "{\"title\":" + quote(title)
                + ",\"body\":" + quote(body)
                + ",\"url\":" + quote(url)
                + ",\"icon\":" + quote(icon)
                + ",\"image\":" + quote(image) + "}";
    }

    static String quote(String s) {
        if (s == null) {
            s = "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                default:
                    // 制御文字と、JavaScriptで行の区切りとして扱われる U+2028 / U+2029 は、4桁の16進数のエスケープ(円記号+u+4桁)にする
                    if (c < 0x20 || c == '\u2028' || c == '\u2029') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
