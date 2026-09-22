package com.example.webapp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Appleの人気楽曲ランキング(rss.marketingtools.apple.com)を取得するサービス。
 *
 * 注意点:
 * ランキングAPIのレスポンスには試聴URL(previewUrl)が含まれていない。
 * そのため、ランキング取得後にiTunes Lookup APIへ曲IDをまとめて問い合わせ、
 * previewUrlを別途取得して合成する2段階の処理になる。
 */
@Service
public class ItunesRankingService {

    private static final Logger log = LoggerFactory.getLogger(ItunesRankingService.class);

    // 日本の人気楽曲トップ10(認証不要、APIキー不要)
    private static final String RANKING_URL =
            "https://rss.marketingtools.apple.com/api/v2/jp/music/most-played/10/songs.json";

    private static final String LOOKUP_BASE_URL = "https://itunes.apple.com/lookup";

    private final RestTemplate restTemplate;

    public ItunesRankingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 人気楽曲ランキングを取得する。
     * previewUrlの取得に失敗した曲があっても、その曲だけpreviewUrl=nullとして返す
     * (ランキング自体は表示できた方が良いため、部分的な失敗で全体を空にはしない)。
     *
     * @return ランキング順の曲リスト。取得自体に失敗した場合は空リストを返す。
     */
    public List<ItunesTrackDto> getPopularSongs() {
        ItunesRankingResponse ranking = fetchRanking();

        if (ranking == null || ranking.getFeed() == null || ranking.getFeed().getResults() == null) {
            // ItunesRankingResponse.Feed が null、または results が null のケースをまとめて弾く
            log.warn("ランキングAPIから空のレスポンスが返却されました。");
            return Collections.emptyList();
        }

        List<ItunesRankingTrackDto> rankingTracks = ranking.getFeed().getResults();
        Map<Long, String> previewUrlByTrackId = fetchPreviewUrls(rankingTracks);

        return normalize(rankingTracks, previewUrlByTrackId);
    }

    /**
     * ランキングAPIを呼び出す。
     */
    private ItunesRankingResponse fetchRanking() {
        try {
            return restTemplate.getForObject(RANKING_URL, ItunesRankingResponse.class);
        } catch (RestClientException e) {
            log.error("ランキングAPI呼び出しに失敗しました。", e);
            return null;
        }
    }

    /**
     * ランキングに含まれる曲のIDをまとめてLookup APIに問い合わせ、
     * "trackId -> previewUrl" のマップを返す。
     * 1曲ずつ問い合わせるとAPI呼び出し回数が増えるため、IDをカンマ区切りでまとめて1回のリクエストにする。
     */
    private Map<Long, String> fetchPreviewUrls(List<ItunesRankingTrackDto> rankingTracks) {
        String ids = rankingTracks.stream()
                .map(ItunesRankingTrackDto::getId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.joining(","));

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        String url = UriComponentsBuilder.fromUriString(LOOKUP_BASE_URL)
                .queryParam("id", ids)
                .queryParam("country", "JP")
                .encode()
                .toUriString();

        try {
            // Lookup APIのレスポンス構造は検索APIと同じ({ resultCount, results }) なので、
            // 既存のItunesSearchResponse/ItunesTrackDtoをそのまま再利用できる
            ItunesSearchResponse response = restTemplate.getForObject(url, ItunesSearchResponse.class);

            if (response == null || response.getResults() == null) {
                return Collections.emptyMap();
            }

            Map<Long, String> previewUrlByTrackId = new HashMap<>();
            for (ItunesTrackDto track : response.getResults()) {
                if (track.getTrackId() != null && track.getPreviewUrl() != null) {
                    previewUrlByTrackId.put(track.getTrackId(), track.getPreviewUrl());
                }
            }
            return previewUrlByTrackId;

        } catch (RestClientException e) {
            // 試聴URLが取れなくてもランキング自体は表示したいので、失敗しても空マップで続行する
            log.error("Lookup API呼び出しに失敗しました。", e);
            return Collections.emptyMap();
        }
    }

    /**
     * ランキングAPI独自のフィールド名({@link ItunesRankingTrackDto})を、
     * アプリ内で共通して使う{@link ItunesTrackDto}の形に詰め替える。
     * previewUrlはLookup APIの結果から補完する。
     */
    private List<ItunesTrackDto> normalize(List<ItunesRankingTrackDto> rankingTracks,
                                            Map<Long, String> previewUrlByTrackId) {
        List<ItunesTrackDto> normalized = new ArrayList<>();

        for (ItunesRankingTrackDto rankingTrack : rankingTracks) {
            Long trackId = parseTrackId(rankingTrack.getId());

            ItunesTrackDto dto = new ItunesTrackDto();
            dto.setTrackId(trackId);
            dto.setTrackName(rankingTrack.getName());
            dto.setArtistName(rankingTrack.getArtistName());
            dto.setArtworkUrl(rankingTrack.getArtworkUrl100());
            dto.setAppleMusicUrl(rankingTrack.getUrl());
            dto.setPreviewUrl(trackId != null ? previewUrlByTrackId.get(trackId) : null);

            normalized.add(dto);
        }

        return normalized;
    }

    private Long parseTrackId(String id) {
        try {
            return id != null ? Long.valueOf(id) : null;
        } catch (NumberFormatException e) {
            log.warn("ランキングAPIのidをLongに変換できませんでした。id={}", id);
            return null;
        }
    }
}