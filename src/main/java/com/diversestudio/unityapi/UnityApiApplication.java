package com.diversestudio.unityapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UnityApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnityApiApplication.class, args);
	}

}
