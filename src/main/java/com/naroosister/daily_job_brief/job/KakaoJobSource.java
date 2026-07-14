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
public class KakaoJobSource implements JobSource {

	private static final String COMPANY = "KAKAO";
	private static final String JOB_LIST_URL = "https://careers.kakao.com/public/api/job-list";
	private static final String JOB_PART = "TECHNOLOGY";

	private final HttpClient httpClient;
	private final KakaoJobParser parser;

	public KakaoJobSource() {
		this(new KakaoJobParser());
	}

	KakaoJobSource(KakaoJobParser parser) {
		this(HttpClient.newHttpClient(), parser);
	}

	KakaoJobSource(HttpClient httpClient, KakaoJobParser parser) {
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

		byte[] firstPage = fetch(1);
		postings.addAll(parse(firstPage));

		int totalPage = parser.totalPage(firstPage);
		for (int page = 2; page <= totalPage; page++) {
			postings.addAll(parse(fetch(page)));
		}

		return postings;
	}

	private byte[] fetch(int page) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(jobsUri(page))
				.timeout(Duration.ofSeconds(20))
				.header("Accept", "application/json")
				.GET()
				.build();
		HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch Kakao jobs. status=" + response.statusCode());
		}

		return response.body();
	}

	private URI jobsUri(int page) {
		return URI.create(JOB_LIST_URL + "?part=" + JOB_PART + "&keyword=&skilset=&page=" + page);
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
