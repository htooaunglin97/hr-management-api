package com.example.hr;

import org.springframework.boot.SpringApplication;

public class TestHrManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(HrManagementApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
