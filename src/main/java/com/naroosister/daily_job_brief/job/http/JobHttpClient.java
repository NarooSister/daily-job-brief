package com.naroosister.daily_job_brief.job.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class JobHttpClient {

	public static final String ACCEPT_JSON = "application/json";
	public static final String ACCEPT_HTML = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	public static final Map<String, String> BROWSER_HEADERS = Map.of(
			"Accept", ACCEPT_HTML,
			"Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
			"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
					+ "(KHTML, like Gecko) Chrome/126.0 Safari/537.36"
	);

	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

	private final HttpClient httpClient;

	public JobHttpClient() {
		this(HttpClient.newHttpClient());
	}

	JobHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public byte[] get(URI uri, String company, String accept) throws IOException, InterruptedException {
		return get(uri, company, Map.of("Accept", accept));
	}

	public byte[] get(URI uri, String company, Map<String, String> headers) throws IOException, InterruptedException {
		HttpRequest.Builder request = HttpRequest.newBuilder(uri)
				.timeout(REQUEST_TIMEOUT)
				.GET();
		headers.forEach(request::header);

		HttpResponse<byte[]> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("Failed to fetch " + company + " jobs. status=" + response.statusCode());
		}
		return response.body();
	}
}
