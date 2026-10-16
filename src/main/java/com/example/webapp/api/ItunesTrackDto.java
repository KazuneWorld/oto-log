package com.example.webapp.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * アプリ内で共通して使う「曲」のDTO。
 * 検索結果・ランキング結果とも、最終的にはこの形に揃える。
 * 検索API/Lookup APIのレスポンスは、このクラスへ直接デシリアライズされる
 * (JSONのキー名と異なる部分だけ @JsonAlias で対応させている)。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItunesTrackDto {

    private Long trackId;
    private String trackName;
    private String artistName;

    @JsonAlias("artworkUrl100")
    private String artworkUrl;

    private String previewUrl;

    @JsonAlias("trackViewUrl")
    private String appleMusicUrl;
}