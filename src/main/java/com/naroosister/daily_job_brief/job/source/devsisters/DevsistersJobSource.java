package com.naroosister.daily_job_brief.job.source.devsisters;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DevsistersJobSource implements JobSource {

	private static final String COMPANY = "DEVSISTERS";
	private static final String JOB_LIST_URL =
			"https://api.greetinghr.com/ats/v1.1/career/workspaces/12227/openings";
	private static final int PAGE_SIZE = 100;
	private static final Map<String, String> REQUEST_HEADERS = Map.of(
			"Accept", JobHttpClient.ACCEPT_JSON,
			"Origin", "https://careers.devsisters.com",
			"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
					+ "(KHTML, like Gecko) Chrome/126.0 Safari/537.36"
	);

	private final JobHttpClient httpClient;
	private final DevsistersJobParser parser;

	public DevsistersJobSource() {
		this(new DevsistersJobParser());
	}

	DevsistersJobSource(DevsistersJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	DevsistersJobSource(JobHttpClient httpClient, DevsistersJobParser parser) {
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

		byte[] firstPage = fetch(0);
		postings.addAll(parse(firstPage));

		int totalPage = parser.totalPage(firstPage);
		for (int page = 1; page < totalPage; page++) {
			postings.addAll(parse(fetch(page)));
		}

		return postings;
	}

	private byte[] fetch(int page) throws IOException, InterruptedException {
		return httpClient.get(jobsUri(page), COMPANY, REQUEST_HEADERS);
	}

	URI jobsUri(int page) {
		return URI.create(JOB_LIST_URL + "?page=" + page + "&pageSize=" + PAGE_SIZE);
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
