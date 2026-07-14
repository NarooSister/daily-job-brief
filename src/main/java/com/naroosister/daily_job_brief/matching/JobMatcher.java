package com.naroosister.daily_job_brief.matching;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.subscriber.Subscriber;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class JobMatcher {

	public List<JobPosting> match(Subscriber subscriber, List<JobPosting> postings) {
		return postings.stream()
				.filter(posting -> matchesAnyKeyword(subscriber, posting))
				.toList();
	}

	private boolean matchesAnyKeyword(Subscriber subscriber, JobPosting posting) {
		// 현재 정책은 상세 본문 분석 없이 제목에 포함된 구독자 키워드만 본다.
		String title = posting.title().toLowerCase(Locale.ROOT);

		return subscriber.keywords().stream()
				.filter(keyword -> !keyword.isBlank())
				.map(keyword -> keyword.toLowerCase(Locale.ROOT))
				.anyMatch(title::contains);
	}
}
