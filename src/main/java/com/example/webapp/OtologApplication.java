package com.example.webapp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan ("com.example.webapp.mapper")
public class OtologApplication {

	public static void main(String[] args) {
		SpringApplication.run(OtologApplication.class, args);
	}

}
