package com.example.CJLInvestimentos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CjlInvestimentosApplication {

	public static void main(String[] args) {
		SpringApplication.run(CjlInvestimentosApplication.class, args);
	}

}
