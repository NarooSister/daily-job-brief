package com.naroosister.daily_job_brief.job.source.toss;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TossJobSource implements JobSource {

	private static final String COMPANY = "TOSS";
	private static final URI JOBS_URI = URI.create("https://api-public.toss.im/api/v3/ipd-eggnog/career/jobs");

	private final JobHttpClient httpClient;
	private final TossJobParser parser;

	public TossJobSource() {
		this(new TossJobParser());
	}

	TossJobSource(TossJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	TossJobSource(JobHttpClient httpClient, TossJobParser parser) {
		this.httpClient = httpClient;
		this.parser = parser;
	}

	@Override
	public String company() {
		return COMPANY;
	}

	@Override
	public List<JobPosting> fetch() throws IOException, InterruptedException {
		return parse(httpClient.get(JOBS_URI, COMPANY, JobHttpClient.ACCEPT_JSON));
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
