package com.naroosister.daily_job_brief.job;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NaverJobSource implements JobSource {

	private static final String COMPANY = "NAVER";
	private static final int PAGE_SIZE = 10;
	private static final String JOB_LIST_URL = "https://recruit.navercorp.com/rcrt/loadJobList.do";

	private final HttpClient httpClient;
	private final NaverJobParser parser;

	public NaverJobSource() {
		this(new NaverJobParser());
	}

	NaverJobSource(NaverJobParser parser) {
		this(HttpClient.newHttpClient(), parser);
	}

	NaverJobSource(HttpClient httpClient, NaverJobParser parser) {
		this.httpClient = httpClient;
		this.parser = parser;
	}

	@Override
	public String company() {
		return COMPANY;
	}

	@Override
	public List<JobPosting> fetch() throws IOException, InterruptedException {
		List<JobPosting> postings = new ArrayList<>();
		int totalSize = Integer.MAX_VALUE;

		for (int firstIndex = 0; firstIndex < totalSize; firstIndex += PAGE_SIZE) {
			byte[] page = fetch(firstIndex);
			List<JobPosting> pagePostings = parse(page);
			postings.addAll(pagePostings);
			totalSize = parser.totalSize(page);

			if (pagePostings.isEmpty()) {
				break;
			}
		}

		return postings;
	}

	private byte[] fetch(int firstIndex) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(jobsUri(firstIndex))
				.timeout(Duration.ofSeconds(20))
				.header("Accept", "application/json")
				.GET()
				.build();
		HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch NAVER jobs. status=" + response.statusCode());
		}

		return response.body();
	}

	private URI jobsUri(int firstIndex) {
		return URI.create(JOB_LIST_URL + "?firstIndex=" + firstIndex);
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
