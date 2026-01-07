package org.example.scrapingtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ScrapingTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScrapingTestApplication.class, args);
	}

}
