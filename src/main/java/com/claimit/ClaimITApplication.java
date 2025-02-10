package com.claimit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class ClaimITApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaimITApplication.class, args);
	}

}
