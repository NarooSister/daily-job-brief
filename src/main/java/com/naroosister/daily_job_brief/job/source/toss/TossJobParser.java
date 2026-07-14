package com.naroosister.daily_job_brief.job.source.toss;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class TossJobParser {

	private static final String COMPANY = "TOSS";
	private static final String COMPANY_DISPLAY_NAME = "토스";
	private static final String LOCATION = "";
	private static final String JOB_DETAIL_URL_PREFIX = "https://toss.im/career/job-detail?gh_jid=";
	private static final String AFFILIATE_METADATA_ID = "4169410003";
	private static final Map<String, Company> COMPANIES = Map.of(
			"토스", new Company("TOSS", "토스"),
			"토스뱅크", new Company("TOSS_BANK", "토스뱅크"),
			"토스증권", new Company("TOSS_SECURITIES", "토스증권"),
			"토스페이먼츠", new Company("TOSS_PAYMENTS", "토스페이먼츠"),
			"토스인슈어런스", new Company("TOSS_INSURANCE", "토스인슈어런스"),
			"토스씨엑스", new Company("TOSS_CX", "토스씨엑스"),
			"토스플레이스", new Company("TOSS_PLACE", "토스플레이스"),
			"토스인컴", new Company("TOSS_INCOME", "토스인컴"),
			"토스인사이트", new Company("TOSS_INSIGHT", "토스인사이트")
	);

	private final ObjectMapper objectMapper;

	TossJobParser() {
		this(new ObjectMapper());
	}

	TossJobParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		JsonNode root = objectMapper.readTree(json);
		JsonNode jobs = root.path("success");
		List<JobPosting> postings = new ArrayList<>();

		if (!jobs.isArray()) {
			return postings;
		}

		for (JsonNode job : jobs) {
			String id = text(job, "id");
			String title = text(job, "title");
			String url = text(job, "absolute_url");
			Company company = company(job);

			if (!id.isBlank() && !title.isBlank()) {
				postings.add(new JobPosting(id, company.id(), company.displayName(), title, url(id, url), LOCATION));
			}
		}

		return postings;
	}

	private Company company(JsonNode job) {
		for (JsonNode metadata : job.path("metadata")) {
			if (AFFILIATE_METADATA_ID.equals(text(metadata, "id"))) {
				return COMPANIES.getOrDefault(text(metadata, "value"), defaultCompany());
			}
		}
		return defaultCompany();
	}

	private Company defaultCompany() {
		return new Company(COMPANY, COMPANY_DISPLAY_NAME);
	}

	private String text(JsonNode node, String fieldName) {
		return node.path(fieldName).asString("").trim();
	}

	private String url(String id, String url) {
		if (!url.isBlank()) {
			return url;
		}
		return JOB_DETAIL_URL_PREFIX + id;
	}

	private record Company(String id, String displayName) {
	}
}
