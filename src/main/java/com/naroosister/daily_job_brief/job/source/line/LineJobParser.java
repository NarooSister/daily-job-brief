package com.naroosister.daily_job_brief.job.source.line;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class LineJobParser {

	private static final String COMPANY = "LINE";
	private static final String COMPANY_DISPLAY_NAME = "LINE";
	private static final String JOB_DETAIL_URL_PREFIX = "https://careers.linecorp.com/ko/jobs/";

	private final ObjectMapper objectMapper;

	LineJobParser() {
		this(new ObjectMapper());
	}

	LineJobParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		JsonNode root = objectMapper.readTree(json);
		JsonNode jobs = root.path("result").path("data").path("allStrapiJobs").path("edges");
		List<JobPosting> postings = new ArrayList<>();

		if (!jobs.isArray()) {
			return postings;
		}

		for (JsonNode edge : jobs) {
			JsonNode job = edge.path("node");
			String id = text(job, "strapiId");
			String title = title(job);

			if (isVisible(job) && !id.isBlank() && !title.isBlank()) {
				postings.add(new JobPosting(
						id,
						COMPANY,
						companyDisplayName(job),
						title,
						JOB_DETAIL_URL_PREFIX + id,
						location(job)
				));
			}
		}

		return postings;
	}

	private boolean isVisible(JsonNode job) {
		return job.path("publish").asBoolean(false) && job.path("is_public").asBoolean(false);
	}

	private String title(JsonNode job) {
		String title = text(job, "title");
		if (!title.isBlank()) {
			return title;
		}
		return text(job, "title_en");
	}

	private String companyDisplayName(JsonNode job) {
		String company = firstName(job.path("companies"));
		if (!company.isBlank()) {
			return company;
		}
		return COMPANY_DISPLAY_NAME;
	}

	private String location(JsonNode job) {
		List<String> cities = new ArrayList<>();
		for (JsonNode city : job.path("cities")) {
			String name = text(city, "name");
			if (!name.isBlank()) {
				cities.add(name);
			}
		}
		return String.join(", ", cities);
	}

	private String firstName(JsonNode nodes) {
		if (!nodes.isArray() || nodes.isEmpty()) {
			return "";
		}
		return text(nodes.get(0), "name");
	}

	private String text(JsonNode node, String fieldName) {
		return node.path(fieldName).asString("").trim();
	}
}
