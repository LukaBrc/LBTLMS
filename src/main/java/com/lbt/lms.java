package com.lbt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class lms {

	public static void main(String[] args) {
		SpringApplication.run(lms.class, args);
	}

}
