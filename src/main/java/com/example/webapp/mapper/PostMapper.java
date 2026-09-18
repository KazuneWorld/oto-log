package com.example.webapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.webapp.entity.MoodTag;
import java.util.List;

@Mapper
public interface PostMapper {
    
    // タグIDでタグ情報を取得する
    List<MoodTag> findByTagId();

}