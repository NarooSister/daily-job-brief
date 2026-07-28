package com.naroosister.daily_job_brief.job.source.smilegate;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class SmilegateJobSourceTest {

	@Test
	void parsesSmilegateAnnounceApiResponse() throws Exception {
		SmilegateJobSource source = new SmilegateJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "count": 1,
				  "announceCount": 1,
				  "announce": [
				    {
				      "announceSeq": 6145,
				      "title": "[AI\uC13C\uD130] AI \uC6F9\uC11C\uBE44\uC2A4 \uAC1C\uBC1C \uB2F4\uB2F9",
				      "companyNm": "\uC2A4\uB9C8\uC77C\uAC8C\uC774\uD2B8",
				      "jobMainNm": "\uAE30\uC220/\uC778\uD504\uB77C",
				      "jobDtlNm": "\uC2DC\uC2A4\uD15C\uAC1C\uBC1C"
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).containsExactly(new JobPosting(
				"6145",
				"SMILEGATE",
				"\uC2A4\uB9C8\uC77C\uAC8C\uC774\uD2B8",
				"[AI\uC13C\uD130] AI \uC6F9\uC11C\uBE44\uC2A4 \uAC1C\uBC1C \uB2F4\uB2F9",
				"https://careers.smilegate.com/apply/announce/view?seq=6145",
				""
		));
	}

	@Test
	void usesGroupCompanyNameAndFallsBackToSmilegate() throws Exception {
		SmilegateJobSource source = new SmilegateJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "announce": [
				    {
				      "announceSeq": 6172,
				      "title": "\uC1FC\uCE20 \uC81C\uC791 \uC9C0\uC6D0",
				      "companyNm": "\uC2A4\uB9C8\uC77C\uAC8C\uC774\uD2B8(\uC0AC\uC5C5\uBD80\uBB38)"
				    },
				    {
				      "announceSeq": 6170,
				      "title": "\uCEE8\uC149 \uC6D0\uD654 \uB2F4\uB2F9"
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings)
				.extracting(JobPosting::companyDisplayName)
				.containsExactly(
						"\uC2A4\uB9C8\uC77C\uAC8C\uC774\uD2B8(\uC0AC\uC5C5\uBD80\uBB38)",
						"\uC2A4\uB9C8\uC77C\uAC8C\uC774\uD2B8"
				);
	}

	@Test
	void ignoresIncompleteJobs() throws Exception {
		SmilegateJobSource source = new SmilegateJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "announce": [
				    {"announceSeq": 1},
				    {"title": "Missing ID"}
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).isEmpty();
	}

	@Test
	void calculatesTotalPagesAndBuildsPageRequest() throws Exception {
		SmilegateJobParser parser = new SmilegateJobParser();
		SmilegateJobSource source = new SmilegateJobSource(parser);

		int totalPage = parser.totalPage("""
				{"announceCount": 137, "announce": []}
				""".getBytes(StandardCharsets.UTF_8), 20);

		assertThat(totalPage).isEqualTo(7);
		assertThat(source.requestBody(3))
				.contains("\"pageIndex\":3")
				.contains("\"pageSize\":20");
	}
}
