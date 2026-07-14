package com.naroosister.daily_job_brief.state;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SentJobState(Map<String, List<String>> subscribers) {

	public static SentJobState empty() {
		return new SentJobState(new LinkedHashMap<>());
	}
}
