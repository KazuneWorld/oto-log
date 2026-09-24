package com.example.webapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.webapp.entity.MoodTag;
import java.util.List;

@Mapper
public interface PostMapper {
    
    // タグIDでタグ情報を取得する
    List<MoodTag> findByTagId();

    //投稿する
    void insertPostId(@Param ("userId") String userId, @Param ("songId") int songId, @Param("moodTagId") int moodTagId);

}