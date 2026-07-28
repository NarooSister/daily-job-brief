package com.naroosister.daily_job_brief.job.source.smilegate;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class SmilegateJobParser {

	private static final String COMPANY = "SMILEGATE";
	private static final String COMPANY_DISPLAY_NAME = "\uC2A4\uB9C8\uC77C\uAC8C\uC774\uD2B8";
	private static final String JOB_DETAIL_URL_PREFIX =
			"https://careers.smilegate.com/apply/announce/view?seq=";

	private final ObjectMapper objectMapper;

	SmilegateJobParser() {
		this(new ObjectMapper());
	}

	SmilegateJobParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		JsonNode jobs = objectMapper.readTree(json).path("announce");
		List<JobPosting> postings = new ArrayList<>();

		if (!jobs.isArray()) {
			return postings;
		}

		for (JsonNode job : jobs) {
			String id = text(job, "announceSeq");
			String title = text(job, "title");

			if (!id.isBlank() && !title.isBlank()) {
				postings.add(new JobPosting(
						id,
						COMPANY,
						companyDisplayName(job),
						title,
						JOB_DETAIL_URL_PREFIX + id,
						""
				));
			}
		}

		return postings;
	}

	int totalPage(byte[] json, int pageSize) throws IOException {
		int total = objectMapper.readTree(json).path("announceCount").asInt(0);
		return Math.max(1, (total + pageSize - 1) / pageSize);
	}

	private String companyDisplayName(JsonNode job) {
		String companyName = text(job, "companyNm");
		if (!companyName.isBlank()) {
			return companyName;
		}
		return COMPANY_DISPLAY_NAME;
	}

	private String text(JsonNode node, String fieldName) {
		return node.path(fieldName).asString("").trim();
	}
}
