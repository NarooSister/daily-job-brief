package com.naroosister.daily_job_brief.job;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DaangnJobSource implements JobSource {

	private static final String COMPANY = "DAANGN";
	private static final URI JOBS_URI = URI.create("https://careers.daangn.com/jobs/");

	private final HttpClient httpClient;
	private final DaangnJobParser parser;

	public DaangnJobSource() {
		this(new DaangnJobParser());
	}

	DaangnJobSource(DaangnJobParser parser) {
		this(HttpClient.newHttpClient(), parser);
	}

	DaangnJobSource(HttpClient httpClient, DaangnJobParser parser) {
		this.httpClient = httpClient;
		this.parser = parser;
	}

	@Override
	public String company() {
		return COMPANY;
	}

	@Override
	public List<JobPosting> fetch() throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(JOBS_URI)
				.GET()
				.build();
		HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch Daangn jobs. status=" + response.statusCode());
		}

		return parse(response.body());
	}

	List<JobPosting> parse(byte[] html) throws IOException {
		return parser.parse(html);
	}

	List<JobPosting> parse(String html) {
		return parser.parse(html);
	}
}
