package com.naroosister.daily_job_brief.job.source.line;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LineJobSource implements JobSource {

	private static final String COMPANY = "LINE";
	private static final URI JOBS_URI = URI.create("https://careers.linecorp.com/page-data/ko/jobs/page-data.json");

	private final JobHttpClient httpClient;
	private final LineJobParser parser;

	public LineJobSource() {
		this(new LineJobParser());
	}

	LineJobSource(LineJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	LineJobSource(JobHttpClient httpClient, LineJobParser parser) {
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
