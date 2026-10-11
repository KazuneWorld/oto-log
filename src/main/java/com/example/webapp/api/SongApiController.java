package com.example.webapp.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 投稿画面(newpost.html)の検索ボックスから、JS(fetch)経由で呼び出されるAPI。
 * ページ全体の再読み込みなしで検索結果を表示するために、JSON形式で返す。
 */
@RestController
public class SongApiController {

    private final ItunesSearchService itunesSearchService;

    public SongApiController(ItunesSearchService itunesSearchService) {
        this.itunesSearchService = itunesSearchService;
    }

    // 例: GET /api/songs/search?keyword=優里
    @GetMapping("/api/songs/search")
    public List<ItunesTrackDto> search(@RequestParam String keyword) {
        return itunesSearchService.searchSongs(keyword);
    }
}