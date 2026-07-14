package com.naroosister.daily_job_brief.matching;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.subscriber.Subscriber;
import java.util.List;
import org.junit.jupiter.api.Test;

class JobMatcherTest {

	private final JobMatcher jobMatcher = new JobMatcher();

	@Test
	void matchesJobsBySubscriberKeywordsInTitle() {
		Subscriber subscriber = new Subscriber(
				"subscriber-a",
				"subscriber-a@example.test",
				List.of("DevOps", "SRE", "Platform Engineer")
		);
		JobPosting sre = new JobPosting("1", "DAANGN", "당근", "SRE Engineer", "https://example.com/1", "Korea");
		JobPosting platform = new JobPosting("2", "DAANGN", "당근", "Senior Platform Engineer", "https://example.com/2", "Korea");
		JobPosting backend = new JobPosting("3", "DAANGN", "당근", "Backend Engineer", "https://example.com/3", "Korea");

		List<JobPosting> matches = jobMatcher.match(subscriber, List.of(sre, platform, backend));

		assertThat(matches).containsExactly(sre, platform);
	}

	@Test
	void doesNotMatchWhenTitleDoesNotContainKeywords() {
		Subscriber subscriber = new Subscriber(
				"subscriber-a",
				"subscriber-a@example.test",
				List.of("DevOps")
		);
		JobPosting posting = new JobPosting("1", "DAANGN", "당근", "Backend Engineer", "https://example.com/1", "Korea");

		List<JobPosting> matches = jobMatcher.match(subscriber, List.of(posting));

		assertThat(matches).isEmpty();
	}

	@Test
	void ignoresBlankKeywords() {
		Subscriber subscriber = new Subscriber(
				"subscriber-a",
				"subscriber-a@example.test",
				List.of(" ", "")
		);
		JobPosting posting = new JobPosting("1", "DAANGN", "당근", "Backend Engineer", "https://example.com/1", "Korea");

		List<JobPosting> matches = jobMatcher.match(subscriber, List.of(posting));

		assertThat(matches).isEmpty();
	}

	@Test
	void matchingIsCaseInsensitive() {
		Subscriber subscriber = new Subscriber(
				"subscriber-b",
				"subscriber-b@example.test",
				List.of("spring")
		);
		JobPosting posting = new JobPosting("1", "DAANGN", "당근", "Java Spring Backend Engineer", "https://example.com/1", "Korea");

		List<JobPosting> matches = jobMatcher.match(subscriber, List.of(posting));

		assertThat(matches).containsExactly(posting);
	}
}
