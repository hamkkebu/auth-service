package com.hamkkebu.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
	"com.hamkkebu.authservice",
	"com.hamkkebu.boilerplate.common",
	"com.hamkkebu.boilerplate.config"
})
@EnableJpaRepositories(basePackages = {
	"com.hamkkebu.authservice.repository",
	"com.hamkkebu.boilerplate.repository"
})
@EntityScan(basePackages = {
	"com.hamkkebu.authservice.data.entity",
	"com.hamkkebu.boilerplate.data.entity"
})
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
