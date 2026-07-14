package com.naroosister.daily_job_brief.email;

public record EmailMessage(
		String to,
		String subject,
		String htmlBody
) {
}
