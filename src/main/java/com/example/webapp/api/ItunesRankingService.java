package com.example.webapp.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
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

    // キャッシュの有効期間(この間は外部APIを呼ばず、前回の結果を使い回す)
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final RestTemplate restTemplate;

    // 複数リクエストが同時に来る可能性があるため volatile にして、
    // 「キャッシュを読む/書く」際に他スレッドから見える状態を最新に保つ
    private volatile List<ItunesTrackDto> cachedSongs = Collections.emptyList();
    private volatile Instant cachedAt = Instant.EPOCH;

    public ItunesRankingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 人気楽曲ランキングを取得する。
     * キャッシュが有効な間はそれを返し、期限切れの場合のみ外部APIへ問い合わせる。
     * 外部API呼び出しに失敗した場合、古いキャッシュが残っていればそれを返す
     * (真っ白な画面よりは、多少古くても表示できた方が体験として良いため)。
     *
     * @return ランキング順の曲リスト。キャッシュも取得もない場合は空リストを返す。
     */
    public List<ItunesTrackDto> getPopularSongs() {
        if (isCacheValid()) {
            return cachedSongs;
        }

        List<ItunesTrackDto> fresh = fetchFromApi();

        if (!fresh.isEmpty()) {
            cachedSongs = fresh;
            cachedAt = Instant.now();
            return fresh;
        }

        // 取得失敗(空リスト)の場合、古いキャッシュがあればそれで代用する
        if (!cachedSongs.isEmpty()) {
            log.warn("最新のランキング取得に失敗したため、キャッシュ(取得日時: {})を返します。", cachedAt);
            return cachedSongs;
        }

        return Collections.emptyList();
    }

    private boolean isCacheValid() {
        return !cachedSongs.isEmpty()
                && Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0;
    }

    /**
     * 外部API(ランキング+Lookup)を実際に呼び出し、正規化した曲リストを返す。
     * 失敗した場合は空リストを返す(呼び出し元でキャッシュへのフォールバックを行う)。
     */
    private List<ItunesTrackDto> fetchFromApi() {
        ItunesRankingResponse ranking = fetchRanking();

        if (ranking == null || ranking.getFeed() == null || ranking.getFeed().getResults() == null) {
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

        URI uri = UriComponentsBuilder.fromUriString(LOOKUP_BASE_URL)
                .queryParam("id", ids)
                .queryParam("country", "JP")
                .build()
                .encode()
                .toUri();

        try {
            // Lookup APIのレスポンス構造は検索APIと同じ({ resultCount, results }) なので、
            // 既存のItunesSearchResponse/ItunesTrackDtoをそのまま再利用できる
            ItunesSearchResponse response = restTemplate.getForObject(uri, ItunesSearchResponse.class);

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