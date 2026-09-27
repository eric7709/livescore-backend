package com.zestio.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.zestio.app")
public class ZestioApplication {
	public static void main(String[] args) {
		SpringApplication.run(ZestioApplication.class, args);
	}

}
