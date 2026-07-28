package com.naroosister.daily_job_brief.job.source.smilegate;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.job.http.JobHttpClient;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SmilegateJobSource implements JobSource {

	private static final String COMPANY = "SMILEGATE";
	private static final URI JOB_LIST_URI =
			URI.create("https://careers.smilegate.com/api/apply/announce/guest");
	private static final int PAGE_SIZE = 20;
	private static final Map<String, String> REQUEST_HEADERS = Map.of(
			"Accept", JobHttpClient.ACCEPT_JSON,
			"Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7",
			"Content-Type", "application/json",
			"User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
					+ "(KHTML, like Gecko) Chrome/126.0 Safari/537.36"
	);

	private final JobHttpClient httpClient;
	private final SmilegateJobParser parser;

	public SmilegateJobSource() {
		this(new SmilegateJobParser());
	}

	SmilegateJobSource(SmilegateJobParser parser) {
		this(new JobHttpClient(), parser);
	}

	SmilegateJobSource(JobHttpClient httpClient, SmilegateJobParser parser) {
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

		int totalPage = parser.totalPage(firstPage, PAGE_SIZE);
		for (int page = 2; page <= totalPage; page++) {
			postings.addAll(parse(fetch(page)));
		}

		return postings;
	}

	private byte[] fetch(int page) throws IOException, InterruptedException {
		return httpClient.post(
				JOB_LIST_URI,
				COMPANY,
				requestBody(page).getBytes(StandardCharsets.UTF_8),
				REQUEST_HEADERS
		);
	}

	String requestBody(int page) {
		return ("{\"careerTypeCd\":[],\"companyCd\":[],\"gameGenreCd\":[],\"hireTypeCd\":[],"
				+ "\"jobDtlCd\":[],\"jobMainCd\":[],\"keyword\":null,\"pageIndex\":%d,"
				+ "\"pageSize\":%d,\"projectSeq\":null,\"usreId\":null}")
				.formatted(page, PAGE_SIZE);
	}

	List<JobPosting> parse(byte[] json) throws IOException {
		return parser.parse(json);
	}
}
