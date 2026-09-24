package com.example.webapp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 人気楽曲ランキングAPIのレスポンス全体を表すDTO。
 * 実際のレスポンスは { "feed": { "results": [ ... ] } } という2階層構造。
 * "feed"の中身しか使わないため、内部クラス(Feed)としてこのファイルにまとめている。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItunesRankingResponse {

    private Feed feed;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Feed {
        private List<ItunesRankingTrackDto> results;
    }
}