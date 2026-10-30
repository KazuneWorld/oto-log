package com.example.webapp.push;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** @Async(別スレッドでの実行)を有効にする。すでにプロジェクトのどこかで @EnableAsync している場合は、このクラスは不要 */
@Configuration
@EnableAsync
public class PushAsyncConfig {
}
