package com.kang.ecommercedataplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EcommerceDataPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceDataPlatformApplication.class, args);
	}

}
