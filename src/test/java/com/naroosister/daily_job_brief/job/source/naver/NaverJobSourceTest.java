package com.naroosister.daily_job_brief.job.source.naver;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class NaverJobSourceTest {

	@Test
	void parsesNaverJobsApiResponse() throws Exception {
		NaverJobSource source = new NaverJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "result": "Y",
				  "list": [
				    {
				      "annoId": 30005124,
				      "sysCompanyCdNm": "NAVER WEBTOON",
				      "annoSubject": "[\\uB124\\uC774\\uBC84\\uC6F9\\uD230] \\uBC31\\uC5D4\\uB4DC \\uC11C\\uBC84 \\uAC1C\\uBC1C (\\uACBD\\uB825)",
				      "stateCd": "0040",
				      "jobDetailLink": "https://recruit.navercorp.com/rcrt/view.do?annoId=30005124"
				    }
				  ],
				  "totalSize": 1
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).containsExactly(new JobPosting(
				"30005124",
				"NAVER_WEBTOON",
				"NAVER WEBTOON",
				"[\uB124\uC774\uBC84\uC6F9\uD230] \uBC31\uC5D4\uB4DC \uC11C\uBC84 \uAC1C\uBC1C (\uACBD\uB825)",
				"https://recruit.navercorp.com/rcrt/view.do?annoId=30005124",
				""
		));
	}

	@Test
	void fallsBackToNaverCompanyAndDetailUrl() throws Exception {
		NaverJobSource source = new NaverJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "list": [
				    {
				      "annoId": 30005098,
				      "annoSubject": "[NAVER] Efficient World Models Research",
				      "stateCd": "0040"
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).singleElement()
				.satisfies(posting -> {
					assertThat(posting.company()).isEqualTo("NAVER");
					assertThat(posting.companyDisplayName()).isEqualTo("NAVER");
					assertThat(posting.url()).isEqualTo("https://recruit.navercorp.com/rcrt/view.do?annoId=30005098");
				});
	}

	@Test
	void ignoresClosedAndIncompleteJobs() throws Exception {
		NaverJobSource source = new NaverJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "list": [
				    {
				      "annoId": 1,
				      "annoSubject": "Closed Backend Engineer",
				      "stateCd": "0090"
				    },
				    {
				      "annoSubject": "Missing ID Engineer",
				      "stateCd": "0040"
				    },
				    {
				      "annoId": 2,
				      "stateCd": "0040"
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).isEmpty();
	}

	@Test
	void parsesTotalSizeForPagedCollection() throws Exception {
		NaverJobParser parser = new NaverJobParser();

		int totalSize = parser.totalSize("""
				{
				  "list": [],
				  "totalSize": 29
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(totalSize).isEqualTo(29);
	}
}
