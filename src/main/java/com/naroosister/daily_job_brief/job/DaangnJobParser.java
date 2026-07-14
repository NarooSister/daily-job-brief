package com.naroosister.daily_job_brief.job;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
class DaangnJobParser {

	private static final String COMPANY = "DAANGN";
	private static final String COMPANY_DISPLAY_NAME = "당근";
	private static final String LOCATION = "";
	private static final URI JOBS_URI = URI.create("https://careers.daangn.com/jobs/");
	private static final Pattern JOB_ID_PATTERN = Pattern.compile("/jobs/role/(\\d+)/");
	// 당근 채용 목록의 링크 텍스트 끝에 붙는 경력/고용형태 라벨은 알림 제목에서 제거한다.
	private static final Pattern TRAILING_METADATA = Pattern.compile(
			"\\s+(신입|경력|경력무관)\\s+(정규직|계약직|인턴)\\s*$"
	);

	List<JobPosting> parse(byte[] html) throws IOException {
		Document document = Jsoup.parse(new ByteArrayInputStream(html), null, JOBS_URI.toString());
		return parse(document);
	}

	List<JobPosting> parse(String html) {
		return parse(Jsoup.parse(html, JOBS_URI.toString()));
	}

	private List<JobPosting> parse(Document document) {
		// 목록 페이지의 상세 공고 링크만 골라 내부 표준 모델(JobPosting)로 변환한다.
		List<JobPosting> postings = new ArrayList<>();

		for (Element link : document.select("a[href*=/jobs/role/]")) {
			String url = link.absUrl("href");
			String id = jobId(url);
			String title = title(link.text());

			if (!id.isBlank() && !title.isBlank()) {
				postings.add(new JobPosting(id, COMPANY, COMPANY_DISPLAY_NAME, title, url, LOCATION));
			}
		}

		return postings;
	}

	private String jobId(String url) {
		// URL의 role 숫자를 회사 내 공고 식별자로 사용한다.
		Matcher matcher = JOB_ID_PATTERN.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	private String title(String text) {
		return TRAILING_METADATA.matcher(text).replaceFirst("").trim();
	}
}
