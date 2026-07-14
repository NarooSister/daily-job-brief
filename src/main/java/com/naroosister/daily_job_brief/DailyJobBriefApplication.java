package com.naroosister.daily_job_brief;

import com.naroosister.daily_job_brief.config.DailyJobBriefProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(DailyJobBriefProperties.class)
public class DailyJobBriefApplication {

	public static void main(String[] args) {
		SpringApplication.run(DailyJobBriefApplication.class, args);
	}

	@Bean
	@ConditionalOnProperty(name = "daily-job-brief.run-on-startup", havingValue = "true", matchIfMissing = true)
	CommandLineRunner commandLineRunner(JobBriefOrchestrator orchestrator) {
		return args -> orchestrator.run();
	}

}
