package com.naroosister.daily_job_brief.config;

import java.util.List;
import java.util.Locale;
import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("daily-job-brief")
public record DailyJobBriefProperties(
		String subscribersPath,
		String statePath,
		Mail mail,
		Sources sources
) {

	public DailyJobBriefProperties {
		if (subscribersPath == null || subscribersPath.isBlank()) {
			subscribersPath = "config/subscribers.json";
		}
		if (statePath == null || statePath.isBlank()) {
			statePath = "state/sent-jobs.json";
		}
		if (mail == null) {
			mail = new Mail(false, null);
		}
		if (sources == null) {
			sources = new Sources(List.of(), List.of());
		}
	}

	public Path subscribersFile() {
		return Path.of(subscribersPath);
	}

	public Path stateFile() {
		return Path.of(statePath);
	}

	public record Mail(
			boolean enabled,
			String from
	) {
		public Mail {
			if (from != null && from.isBlank()) {
				from = null;
			}
		}
	}

	public record Sources(
			List<String> enabled,
			List<String> disabled
	) {
		public Sources {
			enabled = normalize(enabled);
			disabled = normalize(disabled);
		}

		public boolean isEnabled(String company) {
			String normalizedCompany = normalize(company);
			if (disabled.contains(normalizedCompany)) {
				return false;
			}
			return enabled.isEmpty() || enabled.contains(normalizedCompany);
		}

		private static List<String> normalize(List<String> companies) {
			if (companies == null) {
				return List.of();
			}
			return companies.stream()
					.map(Sources::normalize)
					.filter(company -> !company.isBlank())
					.distinct()
					.toList();
		}

		private static String normalize(String company) {
			if (company == null) {
				return "";
			}
			return company.trim().toLowerCase(Locale.ROOT);
		}
	}
}
