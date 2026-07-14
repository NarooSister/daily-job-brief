package com.naroosister.daily_job_brief.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class DaangnJobSourceTest {

	@Test
	void parsesDaangnJobListPageFromString() {
		DaangnJobSource source = new DaangnJobSource();

		List<JobPosting> postings = source.parse("""
				<html>
				  <body>
				    <a href="/jobs/role/5004587003/">
				      Network Engineer - 인프라(네트워크, Cloud) 경력 정규직
				    </a>
				  </body>
				</html>
				""");

		assertThat(postings).containsExactly(new JobPosting(
				"5004587003",
				"DAANGN",
				"Network Engineer - 인프라(네트워크, Cloud)",
				"https://careers.daangn.com/jobs/role/5004587003/",
				"Korea"
		));
	}

	@Test
	void parsesDaangnJobListPageFromBytes() throws Exception {
		DaangnJobSource source = new DaangnJobSource();
		byte[] html = """
				<html>
				  <head>
				    <meta charset="utf-8">
				  </head>
				  <body>
				    <a href="/jobs/role/5004587003/">
				      Network Engineer - 인프라(네트워크, Cloud) 경력 정규직
				    </a>
				  </body>
				</html>
				""".getBytes(StandardCharsets.UTF_8);

		List<JobPosting> postings = source.parse(html);

		assertThat(postings).extracting(JobPosting::title)
				.containsExactly("Network Engineer - 인프라(네트워크, Cloud)");
	}

	@Test
	void ignoresLinksWithoutJobRoleId() {
		DaangnJobSource source = new DaangnJobSource();

		List<JobPosting> postings = source.parse("""
				<html>
				  <body>
				    <a href="/jobs/not-role/5004587003/">Network Engineer 경력 정규직</a>
				    <a href="/jobs/role/not-a-number/">Backend Engineer 경력 정규직</a>
				  </body>
				</html>
				""");

		assertThat(postings).isEmpty();
	}
}
