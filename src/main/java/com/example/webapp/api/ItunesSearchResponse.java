package com.example.webapp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * 検索API/Lookup APIのレスポンス全体を表すDTO。
 * 実際のレスポンスは { "resultCount": 5, "results": [ ... ] } という構造。
 * Lookup APIも同じ構造で返ってくるため、このクラスを両方で再利用している。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItunesSearchResponse {

    private int resultCount;
    private List<ItunesTrackDto> results;
}