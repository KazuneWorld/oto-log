package com.example.webapp.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.webapp.entity.Song;

@Mapper
public interface SongMapper {
    
    //曲情報をDBに登録する（songテーブルに挿入）
    void insertSong(Song song);

    // iTunesの曲IDで登録済みの曲情報を取得する
    Song findByTrackId(String trackId);
}
