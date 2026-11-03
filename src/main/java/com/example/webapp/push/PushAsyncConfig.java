package com.example.webapp.push;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** @Async(別スレッドでの実行)を有効にする。 */
@Configuration
@EnableAsync
public class PushAsyncConfig {
}
