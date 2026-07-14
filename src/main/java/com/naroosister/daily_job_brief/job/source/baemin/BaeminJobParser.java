package com.naroosister.daily_job_brief.job.source.baemin;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class BaeminJobParser {

	private static final String COMPANY = "BAEMIN";
	private static final String COMPANY_DISPLAY_NAME = "\uC6B0\uC544\uD55C\uD615\uC81C\uB4E4";
	private static final String JOB_DETAIL_URL_PREFIX = "https://career.woowahan.com/recruitment/";
	private static final String JOB_DETAIL_URL_SUFFIX = "/detail";
	private static final String LOCATION = "";

	private final ObjectMapper objectMapper;

	BaeminJobParser() {
		this(new ObjectMapper());
	}

	BaeminJobParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		JsonNode root = objectMapper.readTree(json);
		JsonNode jobs = root.path("data").path("list");
		List<JobPosting> postings = new ArrayList<>();

		if (!jobs.isArray()) {
			return postings;
		}

		for (JsonNode job : jobs) {
			String id = text(job, "recruitNumber");
			String title = text(job, "recruitName");

			if (isVisible(job) && !id.isBlank() && !title.isBlank()) {
				postings.add(new JobPosting(
						id,
						COMPANY,
						COMPANY_DISPLAY_NAME,
						title,
						JOB_DETAIL_URL_PREFIX + id + JOB_DETAIL_URL_SUFFIX,
						LOCATION
				));
			}
		}

		return postings;
	}

	int totalPage(byte[] json) throws IOException {
		return Math.max(1, objectMapper.readTree(json).path("data").path("totalPageNumber").asInt(1));
	}

	private boolean isVisible(JsonNode job) {
		return !job.path("recruitDeleteYn").asBoolean(false)
				&& !job.path("isHidden").asBoolean(false)
				&& !job.path("isTemporaryStatus").asBoolean(false)
				&& job.path("isAfterOrEqualOpenDay").asBoolean(true)
				&& !job.path("isAfterOrEqualEndDay").asBoolean(false);
	}

	private String text(JsonNode node, String fieldName) {
		return node.path(fieldName).asString("").trim();
	}
}
