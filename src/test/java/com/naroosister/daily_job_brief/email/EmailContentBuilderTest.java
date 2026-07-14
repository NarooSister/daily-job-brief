package com.naroosister.daily_job_brief.email;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.SourceExecutionReport;
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
				"당근",
				"Software Engineer, Backend",
				"https://careers.daangn.com/jobs/role/1/",
				"Korea"
		);

		EmailMessage message = builder.build(subscriber, List.of(posting));

		assertThat(message.to()).isEqualTo("subscriber-a@example.test");
		assertThat(message.subject()).isEqualTo("[daily-job-brief] New jobs: 1");
		assertThat(message.htmlBody())
				.contains("<h1>New job postings</h1>")
				.contains("<strong>당근</strong>")
				.contains("Software Engineer, Backend")
				.contains("https://careers.daangn.com/jobs/role/1/")
				.doesNotContain("<strong>DAANGN</strong>");
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
				"당근 <Company>",
				"Backend <Java> & Spring",
				"https://example.com/jobs?team=\"backend\"",
				"Korea"
		);

		EmailMessage message = builder.build(subscriber, List.of(posting));

		assertThat(message.htmlBody())
				.contains("당근 &lt;Company&gt;")
				.contains("Backend &lt;Java&gt;")
				.contains("Backend &lt;Java&gt; &amp; Spring")
				.contains("https://example.com/jobs?team=&quot;backend&quot;");
	}

	@Test
	void buildsFailureAlertEmail() {
		EmailMessage message = builder.buildFailureAlert(
				"alerts@example.test",
				List.of(SourceExecutionReport.failure("DAANGN", new RuntimeException("bad <html>")))
		);

		assertThat(message.to()).isEqualTo("alerts@example.test");
		assertThat(message.subject()).isEqualTo("[daily-job-brief] Job source failures: 1");
		assertThat(message.htmlBody())
				.contains("<h1>Job source failures</h1>")
				.contains("DAANGN")
				.contains("java.lang.RuntimeException")
				.contains("bad &lt;html&gt;");
	}

	@Test
	void failureAlertFallsBackToCauseMessage() {
		EmailMessage message = builder.buildFailureAlert(
				"alerts@example.test",
				List.of(SourceExecutionReport.failure("LINE", new RuntimeException(new IllegalStateException("nested error"))))
		);

		assertThat(message.htmlBody())
				.contains("LINE")
				.contains("java.lang.RuntimeException")
				.contains("nested error");
	}
}
