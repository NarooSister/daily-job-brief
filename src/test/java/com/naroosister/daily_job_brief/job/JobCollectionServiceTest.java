package com.naroosister.daily_job_brief.job;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.config.DailyJobBriefProperties;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class JobCollectionServiceTest {

	@Test
	void collectsEnabledSourcesAndDeduplicatesPostings() {
		JobPosting daangn = posting("1", "DAANGN");
		JobPosting duplicateDaangn = posting("1", "DAANGN");
		JobPosting toss = posting("1", "TOSS");
		JobCollectionService service = new JobCollectionService(
				List.of(
						new SuccessfulSource("DAANGN", List.of(daangn, duplicateDaangn)),
						new SuccessfulSource("TOSS", List.of(toss)),
						new SuccessfulSource("LINE", List.of(posting("1", "LINE")))
				),
				properties(new DailyJobBriefProperties.Sources(List.of("daangn", "toss"), List.of()))
		);

		JobCollectionResult result = service.collect();

		assertThat(result.postings()).containsExactly(daangn, toss);
		assertThat(result.reports())
				.extracting(SourceExecutionReport::company)
				.containsExactly("DAANGN", "TOSS");
	}

	@Test
	void excludesDisabledSourcesWhenEnabledListIsEmpty() {
		JobPosting daangn = posting("1", "DAANGN");
		JobCollectionService service = new JobCollectionService(
				List.of(
						new SuccessfulSource("DAANGN", List.of(daangn)),
						new SuccessfulSource("TOSS", List.of(posting("1", "TOSS")))
				),
				properties(new DailyJobBriefProperties.Sources(List.of(), List.of("toss")))
		);

		JobCollectionResult result = service.collect();

		assertThat(result.postings()).containsExactly(daangn);
		assertThat(result.reports())
				.extracting(SourceExecutionReport::company)
				.containsExactly("DAANGN");
	}

	@Test
	void keepsCollectingWhenOneSourceFails() {
		JobPosting toss = posting("1", "TOSS");
		JobCollectionService service = new JobCollectionService(
				List.of(
						new FailingSource("DAANGN"),
						new SuccessfulSource("TOSS", List.of(toss))
				),
				properties(new DailyJobBriefProperties.Sources(List.of(), List.of()))
		);

		JobCollectionResult result = service.collect();

		assertThat(result.postings()).containsExactly(toss);
		assertThat(result.reports()).hasSize(2);
		assertThat(result.reports().get(0).success()).isFalse();
		assertThat(result.reports().get(1).success()).isTrue();
	}

	private static DailyJobBriefProperties properties(DailyJobBriefProperties.Sources sources) {
		return new DailyJobBriefProperties(null, null, null, sources);
	}

	private static JobPosting posting(String id, String company) {
		return new JobPosting(id, company, company + " Backend Engineer", "https://example.com/" + company + "/" + id, "Korea");
	}

	private record SuccessfulSource(String company, List<JobPosting> postings) implements JobSource {

		@Override
		public List<JobPosting> fetch() {
			return postings;
		}
	}

	private record FailingSource(String company) implements JobSource {

		@Override
		public List<JobPosting> fetch() throws IOException {
			throw new IOException("source failed");
		}
	}
}
