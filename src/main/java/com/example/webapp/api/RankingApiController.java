package com.example.webapp.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 投稿画面(newpost.html)がページ表示後にfetchで呼び出す、ランキング取得API。
 *
 * ランキングは主機能ではなく補助的な表示のため、ページの初期表示自体を
 * ランキング取得の完了まで待たせたくない。そのためController側の
 * GET /newpost ではランキングをmodelに含めず、ページ表示後にJS側から
 * このAPIを非同期に呼び出して後から差し込む構成。
 */
@RestController
public class RankingApiController {

    private final ItunesRankingService itunesRankingService;

    public RankingApiController(ItunesRankingService itunesRankingService) {
        this.itunesRankingService = itunesRankingService;
    }

    @GetMapping("/api/songs/ranking")
    public List<ItunesTrackDto> ranking() {
        return itunesRankingService.getPopularSongs();
    }
}