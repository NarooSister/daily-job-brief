package com.naroosister.daily_job_brief.job.source.naver;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NaverJobSource implements JobSource {

	private static final String COMPANY = "NAVER";
	private static final int PAGE_SIZE = 10;
	private static final String JOB_LIST_URL = "https://recruit.navercorp.com/rcrt/loadJobList.do";

	private final JobHttpClient httpClient;
	private final NaverJobParser parser;

	public NaverJobSource() {
		this(new NaverJobParser());
	}

	NaverJobSource(NaverJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	NaverJobSource(JobHttpClient httpClient, NaverJobParser parser) {
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
		int totalSize = Integer.MAX_VALUE;

		for (int firstIndex = 0; firstIndex < totalSize; firstIndex += PAGE_SIZE) {
			byte[] page = fetch(firstIndex);
			List<JobPosting> pagePostings = parse(page);
			postings.addAll(pagePostings);
			totalSize = parser.totalSize(page);

			if (pagePostings.isEmpty()) {
				break;
			}
		}

		return postings;
	}

	private byte[] fetch(int firstIndex) throws IOException, InterruptedException {
		return httpClient.get(jobsUri(firstIndex), COMPANY, JobHttpClient.ACCEPT_JSON);
	}

	private URI jobsUri(int firstIndex) {
		return URI.create(JOB_LIST_URL + "?firstIndex=" + firstIndex);
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
