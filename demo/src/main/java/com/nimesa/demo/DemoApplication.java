package com.nimesa.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
 @ComponentScan(basePackages = {"com.nimesa.demo"})
 @EntityScan(basePackages = {"com.nimesa.demo.entity"})
 @EnableJpaRepositories(basePackages = {"com.nimesa.demo.repository"})
 @EnableAsync
public class DemoApplication {

public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);

	}

}
