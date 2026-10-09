package com.livescore.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EntityScan(basePackages = "com.livescore.app")  
public class LivescoreApplication {
	public static void main(String[] args) {
		SpringApplication.run(LivescoreApplication.class, args);
	}
}