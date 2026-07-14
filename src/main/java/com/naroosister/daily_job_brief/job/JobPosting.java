package com.naroosister.daily_job_brief.job;

public record JobPosting(
		String id,
		String company,
		String companyDisplayName,
		String title,
		String url,
		String location
) {
}
