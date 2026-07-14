package com.naroosister.daily_job_brief.job.source.coupang;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CoupangJobSource implements JobSource {

	private static final String COMPANY = "COUPANG";
	private static final URI JOBS_URI = URI.create("https://www.coupang.jobs/en/jobs/");

	private final JobHttpClient httpClient;
	private final CoupangJobParser parser;

	public CoupangJobSource() {
		this(new CoupangJobParser());
	}

	CoupangJobSource(CoupangJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	CoupangJobSource(JobHttpClient httpClient, CoupangJobParser parser) {
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

		byte[] firstPage = fetch(JOBS_URI);
		postings.addAll(parse(firstPage));

		int totalPage = parser.totalPage(firstPage);
		for (int page = 2; page <= totalPage; page++) {
			postings.addAll(parse(fetch(pageUri(page))));
		}

		return postings;
	}

	private byte[] fetch(URI uri) throws IOException, InterruptedException {
		return httpClient.get(uri, COMPANY, JobHttpClient.BROWSER_HEADERS);
	}

	private URI pageUri(int page) {
		return URI.create(JOBS_URI + "?page=" + page);
	}

	List<JobPosting> parse(byte[] html) throws IOException {
		return parser.parse(html);
	}

	List<JobPosting> parse(String html) {
		return parser.parse(html);
	}
}
