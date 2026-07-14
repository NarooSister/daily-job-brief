package com.naroosister.daily_job_brief.state;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.subscriber.Subscriber;
import java.util.List;
import org.junit.jupiter.api.Test;

class SentJobTrackerTest {

	private final SentJobTracker tracker = new SentJobTracker();

	@Test
	void filtersAlreadySentJobsPerSubscriber() {
		Subscriber subscriber = new Subscriber("subscriber-a", "subscriber-a@example.test", List.of("Software Engineer"));
		JobPosting alreadySent = new JobPosting("1", "DAANGN", "Software Engineer, Backend", "https://example.com/1", "Korea");
		JobPosting newPosting = new JobPosting("2", "DAANGN", "Software Engineer, Frontend", "https://example.com/2", "Korea");
		SentJobState state = new SentJobState(
				java.util.Map.of("subscriber-a", List.of("DAANGN:1"))
		);

		List<JobPosting> unsent = tracker.withoutAlreadySent(state, subscriber, List.of(alreadySent, newPosting));

		assertThat(unsent).containsExactly(newPosting);
	}

	@Test
	void keepsSentJobsSeparateBySubscriber() {
		Subscriber subscriber = new Subscriber("subscriber-b", "subscriber-b@example.test", List.of("Software Engineer"));
		JobPosting posting = new JobPosting("1", "DAANGN", "Software Engineer, Backend", "https://example.com/1", "Korea");
		SentJobState state = new SentJobState(
				java.util.Map.of("subscriber-a", List.of("DAANGN:1"))
		);

		List<JobPosting> unsent = tracker.withoutAlreadySent(state, subscriber, List.of(posting));

		assertThat(unsent).containsExactly(posting);
	}

	@Test
	void marksSentJobsPerSubscriber() {
		Subscriber subscriber = new Subscriber("subscriber-a", "subscriber-a@example.test", List.of("Software Engineer"));
		JobPosting posting = new JobPosting("1", "DAANGN", "Software Engineer, Backend", "https://example.com/1", "Korea");

		SentJobState state = tracker.markSent(SentJobState.empty(), subscriber, List.of(posting));

		assertThat(state.subscribers()).containsEntry("subscriber-a", List.of("DAANGN:1"));
	}

	@Test
	void doesNotDuplicateSentJobKeys() {
		Subscriber subscriber = new Subscriber("subscriber-a", "subscriber-a@example.test", List.of("Software Engineer"));
		JobPosting posting = new JobPosting("1", "DAANGN", "Software Engineer, Backend", "https://example.com/1", "Korea");
		SentJobState existingState = new SentJobState(
				java.util.Map.of("subscriber-a", List.of("DAANGN:1"))
		);

		SentJobState state = tracker.markSent(existingState, subscriber, List.of(posting));

		assertThat(state.subscribers()).containsEntry("subscriber-a", List.of("DAANGN:1"));
	}
}
