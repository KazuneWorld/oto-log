-- ============================================
-- DROP TABLE(開発中の初期化用)
-- 外部キー制約があるため、参照されている側は後に消す
-- 依存関係の深いテーブル → 浅いテーブルの順(CREATEの逆順)
-- ============================================
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS bookmarks;
DROP TABLE IF EXISTS reactions;
DROP TABLE IF EXISTS posts;
DROP TABLE IF EXISTS friends;
DROP TABLE IF EXISTS mood_tags;
DROP TABLE IF EXISTS songs;
DROP TABLE IF EXISTS users;


-- ============================================
-- CREATE TABLE
-- ============================================

-- --------------------------------------------
-- users: ユーザー
-- --------------------------------------------
CREATE TABLE users (
    user_id         VARCHAR(12)  NOT NULL,                -- PK, ランダム生成の管理用id, 12桁英数字
    user_login_id   VARCHAR(30)  NOT NULL,                 -- ユーザーが自由に設定, ログイン専用
    friend_code     VARCHAR(8)   NOT NULL,                 -- ランダム生成, フレンド追加専用, 完全一致検索のみ
    user_email      VARCHAR(255) NOT NULL,                 -- 連絡用
    user_pass_hash  VARCHAR(60)  NOT NULL,                 -- ハッシュ化されたパスワード
    user_nickname   VARCHAR(50)  NOT NULL,                 -- タイムライン表示名
    user_icon       VARCHAR(255) NULL,                     -- ローカル上のファイル名を保存

    PRIMARY KEY (user_id),
    UNIQUE KEY uq_users_login_id (user_login_id),
    UNIQUE KEY uq_users_friend_code (friend_code),
    UNIQUE KEY uq_users_email (user_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- --------------------------------------------
-- songs: 曲情報(iTunes APIから取得)
-- --------------------------------------------
CREATE TABLE songs (
    song_id          INT           NOT NULL AUTO_INCREMENT, -- PK, 内部管理用
    track_id         BIGINT        NOT NULL,                -- iTunesの曲id
    track_name       VARCHAR(255)  NOT NULL,                -- 曲名
    artist_name      VARCHAR(255)  NOT NULL,                -- アーティスト名
    artwork_url      VARCHAR(255)  NULL,                    -- アートワークURL
    preview_url      VARCHAR(255)  NULL,                    -- 試聴URL
    apple_music_url  VARCHAR(255)  NULL,                    -- Apple Musicの再生URL

    PRIMARY KEY (song_id),
    UNIQUE KEY uq_songs_track_id (track_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- --------------------------------------------
-- mood_tags: 気分タグ(マスタ)
-- --------------------------------------------
CREATE TABLE mood_tags (
    mood_tag_id  INT          NOT NULL AUTO_INCREMENT,     -- PK, 内部管理用
    tag_name     VARCHAR(20)  NOT NULL,                    -- 気分の名称

    PRIMARY KEY (mood_tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- --------------------------------------------
-- friends: フレンド関係(申請〜承認の状態を1テーブルで管理)
-- --------------------------------------------
CREATE TABLE friends (
    friend_relation_id  INT          NOT NULL AUTO_INCREMENT, -- PK, 内部管理用
    requester_id        VARCHAR(12)  NOT NULL,                -- 申請した側のuser_id
    receiver_id          VARCHAR(12)  NOT NULL,                -- 申請された側のuser_id
    friend_status        INT          NOT NULL DEFAULT 0,      -- 0=申請中, 1=フレンド状態(却下はレコード削除)
    notify_on_post       BOOLEAN      NOT NULL DEFAULT FALSE,   -- このフレンドの投稿を通知するか

    PRIMARY KEY (friend_relation_id),
    UNIQUE KEY uq_friends_pair (requester_id, receiver_id),
    CONSTRAINT fk_friends_requester FOREIGN KEY (requester_id) REFERENCES users (user_id),
    CONSTRAINT fk_friends_receiver  FOREIGN KEY (receiver_id)  REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- --------------------------------------------
-- posts: 投稿
-- --------------------------------------------
CREATE TABLE posts (
    post_id      VARCHAR(12)  NOT NULL,                    -- PK, 12桁ランダム英数字
    user_id      VARCHAR(12)  NOT NULL,                    -- 投稿者
    song_id      INT          NOT NULL,                    -- 投稿された曲
    mood_tag_id  INT          NOT NULL,                    -- 気分タグ
    posted_at    DATETIME     NOT NULL,                    -- 投稿日時

    PRIMARY KEY (post_id),
    CONSTRAINT fk_posts_user     FOREIGN KEY (user_id)     REFERENCES users (user_id),
    CONSTRAINT fk_posts_song     FOREIGN KEY (song_id)     REFERENCES songs (song_id),
    CONSTRAINT fk_posts_mood_tag FOREIGN KEY (mood_tag_id) REFERENCES mood_tags (mood_tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- --------------------------------------------
-- reactions: 投稿への絵文字リアクション(1投稿につき1人1リアクションまで)
-- --------------------------------------------
CREATE TABLE reactions (
    reaction_id     INT          NOT NULL AUTO_INCREMENT,  -- PK, 内部管理用
    post_id         VARCHAR(12)  NOT NULL,                 -- リアクションされた投稿
    user_id         VARCHAR(12)  NOT NULL,                 -- リアクションした人
    reaction_emoji  VARCHAR(10)  NOT NULL,                 -- 絵文字そのもの
    reacted_at      DATETIME     NOT NULL,                 -- リアクション日時

    PRIMARY KEY (reaction_id),
    UNIQUE KEY uq_reactions_post_user (post_id, user_id),
    CONSTRAINT fk_reactions_post FOREIGN KEY (post_id) REFERENCES posts (post_id),
    CONSTRAINT fk_reactions_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- --------------------------------------------
-- bookmarks: ブックマークした投稿
-- --------------------------------------------
CREATE TABLE bookmarks (
    bookmark_id  INT          NOT NULL AUTO_INCREMENT,     -- PK, 内部管理用
    user_id      VARCHAR(12)  NOT NULL,                    -- ブックマークした人
    post_id      VARCHAR(12)  NOT NULL,                    -- ブックマークされた投稿

    PRIMARY KEY (bookmark_id),
    UNIQUE KEY uq_bookmarks_user_post (user_id, post_id),
    CONSTRAINT fk_bookmarks_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_bookmarks_post FOREIGN KEY (post_id) REFERENCES posts (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- --------------------------------------------
-- notifications: 通知
-- --------------------------------------------
CREATE TABLE notifications (
    notification_id    INT          NOT NULL AUTO_INCREMENT, -- PK, 内部管理用
    recipient_id        VARCHAR(12)  NOT NULL,                -- 通知を受け取る人
    sender_id            VARCHAR(12)  NOT NULL,                -- 通知の発生元となった人
    notification_type    INT          NOT NULL,                -- 0=申請,1=成立,2=リアクション,3=フレンド投稿 etc...
    post_id               VARCHAR(12)  NULL,                    -- 投稿系通知のみ使用
    is_read               BOOLEAN      NOT NULL DEFAULT FALSE,   -- 既読フラグ
    created_at            DATETIME     NOT NULL,                 -- 通知発生日時

    PRIMARY KEY (notification_id),
    CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (user_id),
    CONSTRAINT fk_notifications_sender    FOREIGN KEY (sender_id)    REFERENCES users (user_id),
    CONSTRAINT fk_notifications_post      FOREIGN KEY (post_id)      REFERENCES posts (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;