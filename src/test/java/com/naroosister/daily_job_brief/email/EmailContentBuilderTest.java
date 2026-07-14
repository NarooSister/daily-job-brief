package com.naroosister.daily_job_brief.email;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.subscriber.Subscriber;
import java.util.List;
import org.junit.jupiter.api.Test;

class EmailContentBuilderTest {

	private final EmailContentBuilder builder = new EmailContentBuilder();

	@Test
	void buildsHtmlEmailForNewJobs() {
		Subscriber subscriber = new Subscriber(
				"subscriber-a",
				"subscriber-a@example.test",
				List.of("Software Engineer")
		);
		JobPosting posting = new JobPosting(
				"1",
				"DAANGN",
				"Software Engineer, Backend",
				"https://careers.daangn.com/jobs/role/1/",
				"Korea"
		);

		EmailMessage message = builder.build(subscriber, List.of(posting));

		assertThat(message.to()).isEqualTo("subscriber-a@example.test");
		assertThat(message.subject()).isEqualTo("[daily-job-brief] New jobs: 1");
		assertThat(message.htmlBody())
				.contains("<h1>New job postings</h1>")
				.contains("Software Engineer, Backend")
				.contains("https://careers.daangn.com/jobs/role/1/");
	}

	@Test
	void escapesHtmlContent() {
		Subscriber subscriber = new Subscriber(
				"subscriber-a",
				"subscriber-a@example.test",
				List.of("Backend <Java>")
		);
		JobPosting posting = new JobPosting(
				"1",
				"DAANGN",
				"Backend <Java> & Spring",
				"https://example.com/jobs?team=\"backend\"",
				"Korea"
		);

		EmailMessage message = builder.build(subscriber, List.of(posting));

		assertThat(message.htmlBody())
				.contains("Backend &lt;Java&gt;")
				.contains("Backend &lt;Java&gt; &amp; Spring")
				.contains("https://example.com/jobs?team=&quot;backend&quot;");
	}
}
