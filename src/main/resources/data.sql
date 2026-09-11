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