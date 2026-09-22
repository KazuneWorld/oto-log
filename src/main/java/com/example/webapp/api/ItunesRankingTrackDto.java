package com.example.webapp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * 人気楽曲ランキングAPI(rss.marketingtools.apple.com)のレスポンス1件分を表すDTO。
 * 検索API(ItunesTrackDto)とはフィールド名の体系が異なる点に注意。
 * 例: 曲名は "trackName" ではなく "name"、Apple IDは "id"(文字列)。
 * まだ変換前の生データなので、previewUrlは含まれない(Lookup APIで別途補完する)。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItunesRankingTrackDto {

    private String id;             // Apple ID(Lookup APIに渡すkeyになる)
    private String name;           // 曲名
    private String artistName;     // アーティスト名
    private String artworkUrl100;  // アートワークURL
    private String url;            // 商品ページURL(Apple Musicアプリで開けるリンク)
}