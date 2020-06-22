package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan(basePackages = "com.example.demo.model")
@ComponentScan(basePackages = {"com.*"})
public class Cursospringboot34Application {

	public static void main(String[] args) {
		SpringApplication.run(Cursospringboot34Application.class, args);
	}

}
