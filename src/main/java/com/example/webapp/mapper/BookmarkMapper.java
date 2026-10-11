package com.example.webapp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.webapp.entity.Bookmark;

@Mapper 
public interface BookmarkMapper {
    
    // 投稿をブックマークする
    int addBookmark(@Param("userId") String userId, @Param("postId") String postId);

    // ブックマークを外す
    int removeBookmark(@Param("userId") String userId, @Param("postId") String postId);

    // ブックマーク済みか確認する
    boolean existsBookmark(@Param("userId") String userId, @Param("postId") String postId);

    // 自分のブックマーク一覧を取得する
    List<Bookmark> findBookmarkedPosts(@Param("userId") String userId);

}
