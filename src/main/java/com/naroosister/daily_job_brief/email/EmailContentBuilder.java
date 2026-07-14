package com.naroosister.daily_job_brief.email;

import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.SourceExecutionReport;
import com.naroosister.daily_job_brief.subscriber.Subscriber;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmailContentBuilder {

	public EmailMessage build(Subscriber subscriber, List<JobPosting> postings) {
		String subject = "[daily-job-brief] New jobs: " + postings.size();
		StringBuilder html = new StringBuilder();
		html.append("<!doctype html>");
		html.append("<html><body>");
		appendHeader(html, subscriber);
		appendPostings(html, postings);
		html.append("</body></html>");

		return new EmailMessage(subscriber.email(), subject, html.toString());
	}

	public EmailMessage buildFailureAlert(String to, List<SourceExecutionReport> failures) {
		String subject = "[daily-job-brief] Job source failures: " + failures.size();
		StringBuilder html = new StringBuilder();
		html.append("<!doctype html>");
		html.append("<html><body>");
		html.append("<h1>Job source failures</h1>");
		html.append("<p>Some job sources failed during collection.</p>");
		appendFailures(html, failures);
		html.append("</body></html>");

		return new EmailMessage(to, subject, html.toString());
	}

	private void appendHeader(StringBuilder html, Subscriber subscriber) {
		html.append("<h1>New job postings</h1>");
		html.append("<p>Matched keywords: ")
				.append(escape(String.join(", ", subscriber.keywords())))
				.append("</p>");
	}

	private void appendPostings(StringBuilder html, List<JobPosting> postings) {
		html.append("<ul>");
		for (JobPosting posting : postings) {
			appendPosting(html, posting);
		}
		html.append("</ul>");
	}

	private void appendPosting(StringBuilder html, JobPosting posting) {
		html.append("<li>");
		html.append("<strong>").append(escape(posting.companyDisplayName())).append("</strong>");
		html.append(" - ");
		html.append("<a href=\"").append(escapeAttribute(posting.url())).append("\">")
				.append(escape(posting.title()))
				.append("</a>");
		if (!posting.location().isBlank()) {
			html.append(" (").append(escape(posting.location())).append(")");
		}
		html.append("</li>");
	}

	private void appendFailures(StringBuilder html, List<SourceExecutionReport> failures) {
		html.append("<ul>");
		for (SourceExecutionReport failure : failures) {
			html.append("<li>");
			html.append("<strong>").append(escape(failure.company())).append("</strong>");
			html.append(" - ");
			html.append(escape(failure.errorSummary()));
			html.append("</li>");
		}
		html.append("</ul>");
	}

	private String escape(String value) {
		if (value == null) {
			return "";
		}
		return value
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;");
	}

	private String escapeAttribute(String value) {
		return escape(value).replace("\"", "&quot;");
	}
}
