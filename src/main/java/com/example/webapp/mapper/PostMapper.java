package com.example.webapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.webapp.entity.MoodTag;
import java.util.List;

@Mapper
public interface PostMapper {
    
    // すべてのタグ情報を取得する
    List<MoodTag> findAllTagId();

    //投稿する
    void insertPostId(@Param("postId") String postId, @Param ("userId") String userId, @Param("moodTagId") int moodTagId);

    //投稿ID重複チェック
    boolean existsByPostId(@Param("postId") String postId);

}