package com.naroosister.daily_job_brief.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class KakaoJobParser {

	private static final String COMPANY = "KAKAO";
	private static final String COMPANY_DISPLAY_NAME = "\uCE74\uCE74\uC624";
	private static final String JOB_DETAIL_URL_PREFIX = "https://careers.kakao.com/jobs/";

	private final ObjectMapper objectMapper;

	KakaoJobParser() {
		this(new ObjectMapper());
	}

	KakaoJobParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		JsonNode root = objectMapper.readTree(json);
		JsonNode jobs = root.path("jobList");
		List<JobPosting> postings = new ArrayList<>();

		if (!jobs.isArray()) {
			return postings;
		}

		for (JsonNode job : jobs) {
			String id = id(job);
			String title = text(job, "jobOfferTitle");

			if (isVisible(job) && !id.isBlank() && !title.isBlank()) {
				postings.add(new JobPosting(
						id,
						COMPANY,
						companyDisplayName(job),
						title,
						JOB_DETAIL_URL_PREFIX + id,
						text(job, "locationName")
				));
			}
		}

		return postings;
	}

	int totalPage(byte[] json) throws IOException {
		return Math.max(1, objectMapper.readTree(json).path("totalPage").asInt(1));
	}

	private boolean isVisible(JsonNode job) {
		return job.path("useFlag").asBoolean(true)
				&& !job.path("privateFlag").asBoolean(false)
				&& !job.path("closeFlag").asBoolean(false)
				&& "PROGRESS".equals(text(job, "statusCode"));
	}

	private String id(JsonNode job) {
		String realId = text(job, "realId");
		if (!realId.isBlank()) {
			return realId;
		}
		return text(job, "jobOfferId");
	}

	private String companyDisplayName(JsonNode job) {
		String companyName = text(job, "companyName");
		if (!companyName.isBlank()) {
			return companyName;
		}
		return COMPANY_DISPLAY_NAME;
	}

	private String text(JsonNode node, String fieldName) {
		return node.path(fieldName).asString("").trim();
	}
}
