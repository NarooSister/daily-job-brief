package com.naroosister.daily_job_brief.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class JsonConfig {

	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
