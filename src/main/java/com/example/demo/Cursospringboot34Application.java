package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = "com.example.demo.model")
public class Cursospringboot34Application {

	public static void main(String[] args) {
		SpringApplication.run(Cursospringboot34Application.class, args);
	}

}
