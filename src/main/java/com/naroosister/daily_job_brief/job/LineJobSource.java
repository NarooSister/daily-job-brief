package com.naroosister.daily_job_brief.job;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LineJobSource implements JobSource {

	private static final String COMPANY = "LINE";
	private static final URI JOBS_URI = URI.create("https://careers.linecorp.com/page-data/ko/jobs/page-data.json");

	private final HttpClient httpClient;
	private final LineJobParser parser;

	public LineJobSource() {
		this(new LineJobParser());
	}

	LineJobSource(LineJobParser parser) {
		this(HttpClient.newHttpClient(), parser);
	}

	LineJobSource(HttpClient httpClient, LineJobParser parser) {
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
				.timeout(Duration.ofSeconds(20))
				.header("Accept", "application/json")
				.GET()
				.build();
		HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch LINE jobs. status=" + response.statusCode());
		}

		return parse(response.body());
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
