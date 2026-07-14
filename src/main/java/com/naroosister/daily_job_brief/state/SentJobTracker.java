package com.naroosister.daily_job_brief.state;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.subscriber.Subscriber;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class SentJobTracker {

	public List<JobPosting> withoutAlreadySent(
			SentJobState state,
			Subscriber subscriber,
			List<JobPosting> postings
	) {
		// 같은 공고라도 구독자마다 발송 여부가 다르므로 구독자 ID 기준으로 이력을 조회한다.
		Set<String> sentJobKeys = sentJobKeys(state, subscriber);
		return postings.stream()
				.filter(posting -> !sentJobKeys.contains(jobKey(posting)))
				.toList();
	}

	public SentJobState markSent(
			SentJobState state,
			Subscriber subscriber,
			List<JobPosting> postings
	) {
		// 기존 상태를 직접 바꾸지 않고 새 상태를 만들어 저장 단계에서 한 번에 기록한다.
		Map<String, List<String>> subscribers = new LinkedHashMap<>(state.subscribers());
		Set<String> sentJobKeys = new LinkedHashSet<>(subscribers.getOrDefault(subscriber.id(), List.of()));
		postings.stream()
				.map(this::jobKey)
				.forEach(sentJobKeys::add);
		subscribers.put(subscriber.id(), new ArrayList<>(sentJobKeys));
		return new SentJobState(subscribers);
	}

	String jobKey(JobPosting posting) {
		// 회사가 달라도 공고 ID가 겹칠 수 있어 company:id 형태로 중복 키를 만든다.
		return posting.company() + ":" + posting.id();
	}

	private Set<String> sentJobKeys(SentJobState state, Subscriber subscriber) {
		return new LinkedHashSet<>(state.subscribers().getOrDefault(subscriber.id(), List.of()));
	}
}
