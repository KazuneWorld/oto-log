-- サンプルユーザ（仮）
-- user_id / friend_code は本来ランダム生成だが、初期データ用に固定値を割り当て
INSERT INTO users (
    user_id,
    user_login_id,
    friend_code,
    user_email,
    user_pass_hash,
    user_nickname,
    user_icon
) VALUES (
    'AAA111BBB222',                                                  -- user_id (12桁)
    'otolog',                                                        -- user_login_id
    'ABCD1234',                                                      -- friend_code (8桁)
    'example1@example.com',                                          -- user_email
    '$2a$08$rW06PhwvmUPBgWcSf.Wcq.xGmDkxhNcG3v43vNYdGM8R5kzNIy73q', -- user_pass_hash
    'おとログ',                                                       -- user_nickname
    NULL                                                              -- user_icon
),
(
    'htsrRFNUoNxJ',                                                  -- user_id (12桁)
    'otolog2',                                                        -- user_login_id
    'bHzHur2o',                                                      -- friend_code (8桁)
    'example2@example.com',                                          -- user_email
    '$2a$10$/sI4nZK4Vz9lI85E4Mr4k.lBp334erl267.NPJlok2usoaigrD0YO', -- user_pass_hash
    'おとログ2',                                                       -- user_nickname
    NULL                                                              -- user_icon
),
(
    'AAA111BBB223',                                                  -- user_id (12桁)
    'otolog3',                                                        -- user_login_id
    'EFGH5678',                                                      -- friend_code (8桁)
    'example3@example.com',                                          -- user_email
    '$2a$08$rW06PhwvmUPBgWcSf.Wcq.xGmDkxhNcG3v43vNYdGM8R5kzNIy73q', -- user_pass_hash
    'おとログ3',                                                       -- user_nickname
    NULL                                                              -- user_icon
);

-- 気分タグ 初期データ
INSERT INTO mood_tags (tag_name) VALUES
    ('リラックス'),
    ('元気'),
    ('悲しい'),
    ('チル');

-- サンプル曲情報 初期データ
INSERT INTO songs (
    song_id,
    track_id,
    track_name,
    artist_name,
    artwork_url,
    preview_url,
    apple_music_url
) VALUES (
    '1',
    '1739088799',
    'ライラック',
    'Mrs. GREEN APPLE',
    'https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/4c/3b/b2/4c3bb247-3be8-0c57-aa9a-7f1775a7b7a8/24UMGIM32931.rgb.jpg/100x100bb.jpg',
    'https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview221/v4/0a/c4/5d/0ac45d4d-62ad-9609-4528-78c849392f49/mzaf_15781851599354670952.plus.aac.p.m4a',
    'https://music.apple.com/jp/album/%E3%83%A9%E3%82%A4%E3%83%A9%E3%83%83%E3%82%AF/1739088465?i=1739088799&uo=4'
);

-- サンプル投稿 初期データ
INSERT INTO posts (
    post_id,
    user_id,
    song_id,
    mood_tag_id,
    posted_at
) VALUES (
    'AAA111BBB222',
    'AAA111BBB222',
    '1',
    '1',
    NOW()
);

-- サンプルフレンド 初期データ1（おとログ1とおとログ2が友達状態）
INSERT INTO friends (
    requester_id,
    receiver_id,
    friend_status,
    requester_notify_on_post,
    receiver_notify_on_post
) VALUES (
    'AAA111BBB222',
    'htsrRFNUoNxJ',
    '1',
    TRUE,
    TRUE
);

-- サンプルフレンド 初期データ2（おとログ3さんがおとログ1さんにフレンド申請中）
INSERT INTO friends (
    requester_id,
    receiver_id,
    friend_status,
    requester_notify_on_post,
    receiver_notify_on_post
) VALUES (
    'AAA111BBB223',
    'AAA111BBB222',
    '0',
    FALSE,
    FALSE
);

-- サンプルリアクション 初期データ（一日前に初期投稿におとログ2さんがリアクションをした）
INSERT INTO reactions (post_id, user_id, reaction_emoji, reacted_at) VALUES
    ('AAA111BBB222', 'htsrRFNUoNxJ', '❤️', TIMESTAMPADD(DAY,  -1, CURRENT_TIMESTAMP));

-- サンプル通知 初期データ1（一日前におとログ2さんがリアクションしたことをおとログ1さんに通知する）
INSERT INTO notifications (recipient_id, sender_id, notification_type, post_id, is_read, created_at) VALUES
    ('AAA111BBB222', 'htsrRFNUoNxJ', '2', 'AAA111BBB222', FALSE, TIMESTAMPADD(DAY,  -1, CURRENT_TIMESTAMP));

-- サンプル通知 初期データ2（一日前におとログ3さんがフレンド申請したことをおとログ1さんに通知する）
INSERT INTO notifications (recipient_id, sender_id, notification_type, post_id, is_read, created_at) VALUES
    ('AAA111BBB222', 'AAA111BBB223', '0', NULL, FALSE, TIMESTAMPADD(DAY,  -1, CURRENT_TIMESTAMP));