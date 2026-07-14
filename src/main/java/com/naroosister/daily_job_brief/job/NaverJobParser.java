package com.naroosister.daily_job_brief.job;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class NaverJobParser {

	private static final String COMPANY = "NAVER";
	private static final String COMPANY_DISPLAY_NAME = "NAVER";
	private static final String JOB_DETAIL_URL_PREFIX = "https://recruit.navercorp.com/rcrt/view.do?annoId=";
	private static final String LOCATION = "";

	private final ObjectMapper objectMapper;

	NaverJobParser() {
		this(new ObjectMapper());
	}

	NaverJobParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		JsonNode root = objectMapper.readTree(json);
		JsonNode jobs = root.path("list");
		List<JobPosting> postings = new ArrayList<>();

		if (!jobs.isArray()) {
			return postings;
		}

		for (JsonNode job : jobs) {
			String id = text(job, "annoId");
			String title = text(job, "annoSubject");
			String companyDisplayName = companyDisplayName(job);

			if (!id.isBlank() && !title.isBlank() && isOpen(job)) {
				postings.add(new JobPosting(
						id,
						companyId(companyDisplayName),
						companyDisplayName,
						title,
						url(id, text(job, "jobDetailLink")),
						LOCATION
				));
			}
		}

		return postings;
	}

	int totalSize(byte[] json) throws IOException {
		return Math.max(0, objectMapper.readTree(json).path("totalSize").asInt(0));
	}

	private boolean isOpen(JsonNode job) {
		String stateCode = text(job, "stateCd");
		return stateCode.isBlank() || "0040".equals(stateCode);
	}

	private String companyDisplayName(JsonNode job) {
		String companyName = text(job, "sysCompanyCdNm");
		if (!companyName.isBlank()) {
			return companyName;
		}
		return COMPANY_DISPLAY_NAME;
	}

	private String companyId(String companyDisplayName) {
		if (companyDisplayName.equalsIgnoreCase(COMPANY_DISPLAY_NAME)) {
			return COMPANY;
		}

		String normalized = Normalizer.normalize(companyDisplayName, Normalizer.Form.NFD)
				.replaceAll("\\p{M}", "")
				.replace("&", "AND")
				.replaceAll("[^\\p{Alnum}]+", "_")
				.replaceAll("^_+|_+$", "")
				.toUpperCase(Locale.ROOT);

		if (normalized.isBlank()) {
			return COMPANY;
		}
		return normalized.startsWith(COMPANY + "_") ? normalized : COMPANY + "_" + normalized;
	}

	private String url(String id, String url) {
		if (!url.isBlank()) {
			return url;
		}
		return JOB_DETAIL_URL_PREFIX + id;
	}

	private String text(JsonNode node, String fieldName) {
		return node.path(fieldName).asString("").trim();
	}
}
