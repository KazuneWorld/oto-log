package com.example.webapp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

/**
 * iTunes Search APIを呼び出して曲を検索するサービス。
 * 参考: https://developer.apple.com/library/archive/documentation/AudioVideo/Conceptual/iTuneSearchAPI/
 */
@Service
public class ItunesSearchService {

    private static final Logger log = LoggerFactory.getLogger(ItunesSearchService.class);

    // iTunes Search APIはAPIキー不要で呼び出せる
    private static final String ITUNES_SEARCH_BASE_URL = "https://itunes.apple.com/search";

    // 1回の検索で取得する最大件数
    private static final int SEARCH_LIMIT = 25;

    private final RestTemplate restTemplate;

    // country はJP固定
    @Value("${itunes.search.country:JP}")
    private String country;

    public ItunesSearchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * キーワードから曲を検索する。
     *
     * @param keyword 検索キーワード(曲名・アーティスト名など、ユーザーが入力した文字列)
     * @return 検索結果の曲リスト。ヒットしない場合や取得失敗時は空リストを返す。
     */
    public List<ItunesTrackDto> searchSongs(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            // 空文字での検索はAPIを叩くだけ無駄なので、ここで弾く
            return Collections.emptyList();
        }

        String url = UriComponentsBuilder.fromUriString(ITUNES_SEARCH_BASE_URL)
                .queryParam("term", keyword)
                .queryParam("country", country)
                .queryParam("media", "music")
                .queryParam("entity", "song")
                .queryParam("limit", SEARCH_LIMIT)
                .encode() // 日本語や記号を含むキーワードをURLエンコードする
                .toUriString();

        try {
            ItunesSearchResponse response = restTemplate.getForObject(url, ItunesSearchResponse.class);

            if (response == null || response.getResults() == null) {
                log.warn("iTunes Search APIから空のレスポンスが返却されました。keyword={}", keyword);
                return Collections.emptyList();
            }

            return response.getResults();

        } catch (RestClientException e) {
            // ネットワークエラーやiTunes側の障害時にアプリ全体を落とさないよう、ここで吸収する
            log.error("iTunes Search API呼び出しに失敗しました。keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }
}