package com.example.demo_ibm_mq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.demo_ibm_mq")
public class DemoIbmMqApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoIbmMqApplication.class, args);
	}

}
