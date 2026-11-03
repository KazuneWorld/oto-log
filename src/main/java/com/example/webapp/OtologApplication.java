package com.example.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class OtologApplication {

	public static void main(String[] args) {
		SpringApplication.run(OtologApplication.class, args);
	}

	//起動完了が分かりやすいようにログを出力する
	@EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
        System.out.println("========================================================================================================================================================================================================");
        System.out.println("                                                                               アプリケーションの起動が完了しました！");
		System.out.println("                                                                               http://localhost:8080/ にアクセスしてください。");
        System.out.println("========================================================================================================================================================================================================");
	}
}
