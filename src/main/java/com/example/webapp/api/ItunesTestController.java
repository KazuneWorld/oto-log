package com.example.webapp.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 動作確認専用の一時的なController。
 * ブラウザで直接アクセスして、JSON形式でレスポンスを目視確認できる。
 *
 * 確認できたら削除してOK(本番の画面表示にはThymeleafの@Controllerを別途使う想定)。
 */
@RestController
public class ItunesTestController {

    private final ItunesSearchService itunesSearchService;
    private final ItunesRankingService itunesRankingService;

    public ItunesTestController(ItunesSearchService itunesSearchService,
                                 ItunesRankingService itunesRankingService) {
        this.itunesSearchService = itunesSearchService;
        this.itunesRankingService = itunesRankingService;
    }

    // 例: http://localhost:8080/test/search?keyword=優里
    @GetMapping("/test/search")
    public List<ItunesTrackDto> testSearch(@RequestParam String keyword) {
        return itunesSearchService.searchSongs(keyword);
    }

    // 例: http://localhost:8080/test/ranking
    @GetMapping("/test/ranking")
    public List<ItunesTrackDto> testRanking() {
        return itunesRankingService.getPopularSongs();
    }
}