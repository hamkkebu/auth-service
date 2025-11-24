package com.hamkkebu.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
	"com.hamkkebu.authservice",
	"com.hamkkebu.boilerplate"
}, excludeFilters = @ComponentScan.Filter(
	type = FilterType.ASPECTJ,
	pattern = {
		"com.hamkkebu.boilerplate.service.*",
		"com.hamkkebu.boilerplate.controller.*",
		"com.hamkkebu.boilerplate.listener.*",
		"com.hamkkebu.boilerplate.data.*",
		"com.hamkkebu.boilerplate.repository.*"
	}
))
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
