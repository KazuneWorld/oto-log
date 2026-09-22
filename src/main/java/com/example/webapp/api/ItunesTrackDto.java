package com.example.webapp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * アプリ内で共通して使う「曲」のDTO。
 * 検索結果・ランキング結果とも、最終的にはこの形に揃える。
 * 検索API/Lookup APIのレスポンスは、このクラスへ直接デシリアライズされる
 * (JSONのキー名と異なる部分だけ @JsonProperty で対応させている)。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItunesTrackDto {

    @JsonProperty("trackId")
    private Long trackId;

    @JsonProperty("trackName")
    private String trackName;

    @JsonProperty("artistName")
    private String artistName;

    @JsonProperty("artworkUrl100")
    private String artworkUrl;

    @JsonProperty("previewUrl")
    private String previewUrl;

    @JsonProperty("trackViewUrl")
    private String appleMusicUrl;
}