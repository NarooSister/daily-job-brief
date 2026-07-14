package com.naroosister.daily_job_brief.subscriber;

import java.util.List;

public record Subscriber(
		String id,
		String email,
		List<String> keywords
) {
}
