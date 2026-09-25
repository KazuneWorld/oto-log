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
    'POST00000001',
    'AAA111BBB222',
    '1',
    '1',
    NOW()
);