package com.naroosister.daily_job_brief.job.source.kakao;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KakaoJobSource implements JobSource {

	private static final String COMPANY = "KAKAO";
	private static final String JOB_LIST_URL = "https://careers.kakao.com/public/api/job-list";
	private static final String JOB_PART = "TECHNOLOGY";

	private final JobHttpClient httpClient;
	private final KakaoJobParser parser;

	public KakaoJobSource() {
		this(new KakaoJobParser());
	}

	KakaoJobSource(KakaoJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	KakaoJobSource(JobHttpClient httpClient, KakaoJobParser parser) {
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

		byte[] firstPage = fetch(1);
		postings.addAll(parse(firstPage));

		int totalPage = parser.totalPage(firstPage);
		for (int page = 2; page <= totalPage; page++) {
			postings.addAll(parse(fetch(page)));
		}

		return postings;
	}

	private byte[] fetch(int page) throws IOException, InterruptedException {
		return httpClient.get(jobsUri(page), COMPANY, JobHttpClient.ACCEPT_JSON);
	}

	private URI jobsUri(int page) {
		return URI.create(JOB_LIST_URL + "?part=" + JOB_PART + "&keyword=&skilset=&page=" + page);
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
