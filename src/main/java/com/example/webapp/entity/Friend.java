package com.example.webapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friend {
    
    private int friendRelationId; // フレンド関係のID（Friendテーブルの主キーID）
    private String userId;        // フレンド(相手)のuser_id。プロフィール遷移用
    private String userIcon; // フレンドのユーザーアイコン
    private String userNickname; // フレンドのニックネーム
    
}
