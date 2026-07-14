package com.naroosister.daily_job_brief.job.source.baemin;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BaeminJobSource implements JobSource {

	private static final String COMPANY = "BAEMIN";
	private static final String JOB_LIST_URL = "https://career.woowahan.com/w1/recruits";
	private static final int PAGE_SIZE = 100;

	private final JobHttpClient httpClient;
	private final BaeminJobParser parser;

	public BaeminJobSource() {
		this(new BaeminJobParser());
	}

	BaeminJobSource(BaeminJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	BaeminJobSource(JobHttpClient httpClient, BaeminJobParser parser) {
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
		return httpClient.get(jobsUri(page), COMPANY, JobHttpClient.ACCEPT_JSON);
	}

	private URI jobsUri(int page) {
		return URI.create(JOB_LIST_URL + "?page=" + page + "&size=" + PAGE_SIZE);
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
