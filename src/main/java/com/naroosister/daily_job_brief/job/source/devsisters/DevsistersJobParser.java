package com.naroosister.daily_job_brief.job.source.devsisters;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class DevsistersJobParser {

	private static final String COMPANY = "DEVSISTERS";
	private static final String COMPANY_DISPLAY_NAME = "\uB370\uBE0C\uC2DC\uC2A4\uD130\uC988";
	private static final String JOB_DETAIL_URL_PREFIX = "https://careers.devsisters.com/ko/o/";

	private final ObjectMapper objectMapper;

	DevsistersJobParser() {
		this(new ObjectMapper());
	}

	DevsistersJobParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		JsonNode jobs = objectMapper.readTree(json).path("data").path("datas");
		List<JobPosting> postings = new ArrayList<>();

		if (!jobs.isArray()) {
			return postings;
		}

		for (JsonNode job : jobs) {
			String id = text(job, "openingId");
			String title = text(job, "title");

			if (job.path("deploy").asBoolean(true) && !id.isBlank() && !title.isBlank()) {
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

	int totalPage(byte[] json) throws IOException {
		return Math.max(1, objectMapper.readTree(json).path("data").path("totalPage").asInt(1));
	}

	private String companyDisplayName(JsonNode job) {
		String groupName = text(job.path("group"), "name");
		return groupName.isBlank() ? COMPANY_DISPLAY_NAME : groupName;
	}

	private String location(JsonNode job) {
		JsonNode positions = job.path("openingJobPosition").path("openingJobPositions");
		if (!positions.isArray()) {
			return "";
		}

		for (JsonNode position : positions) {
			JsonNode workplace = position.path("workspacePlace");
			String location = text(workplace, "location");
			if (!location.isBlank()) {
				return location;
			}

			String place = text(workplace, "place");
			if (!place.isBlank()) {
				return place;
			}
		}
		return "";
	}

	private String text(JsonNode node, String fieldName) {
		return node.path(fieldName).asString("").trim();
	}
}
