package com.naroosister.daily_job_brief.job.source.daangn;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DaangnJobSource implements JobSource {

	private static final String COMPANY = "DAANGN";
	private static final URI JOBS_URI = URI.create("https://careers.daangn.com/jobs/");

	private final JobHttpClient httpClient;
	private final DaangnJobParser parser;

	public DaangnJobSource() {
		this(new DaangnJobParser());
	}

	DaangnJobSource(DaangnJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	DaangnJobSource(JobHttpClient httpClient, DaangnJobParser parser) {
		this.httpClient = httpClient;
		this.parser = parser;
	}

	@Override
	public String company() {
		return COMPANY;
	}

	@Override
	public List<JobPosting> fetch() throws IOException, InterruptedException {
		return parse(httpClient.get(JOBS_URI, COMPANY, JobHttpClient.ACCEPT_HTML));
	}

	List<JobPosting> parse(byte[] html) throws IOException {
		return parser.parse(html);
	}

	List<JobPosting> parse(String html) {
		return parser.parse(html);
	}
}
