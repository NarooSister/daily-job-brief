package com.naroosister.daily_job_brief.job.source.kakao;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class KakaoJobSourceTest {

	@Test
	void parsesKakaoJobsApiResponse() throws Exception {
		KakaoJobSource source = new KakaoJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "jobList": [
				    {
				      "realId": "P-14476",
				      "jobOfferId": 14476,
				      "privateFlag": false,
				      "closeFlag": false,
				      "useFlag": true,
				      "jobOfferTitle": "LLM Research Engineer (Pre-training)",
				      "companyName": "\\uCE74\\uCE74\\uC624",
				      "locationName": "\\uD310\\uAD50",
				      "statusCode": "PROGRESS"
				    }
				  ],
				  "totalJobCount": 1,
				  "totalPage": 1
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).containsExactly(new JobPosting(
				"P-14476",
				"KAKAO",
				"\uCE74\uCE74\uC624",
				"LLM Research Engineer (Pre-training)",
				"https://careers.kakao.com/jobs/P-14476",
				"\uD310\uAD50"
		));
	}

	@Test
	void fallsBackToJobOfferIdAndDefaultCompanyDisplayName() throws Exception {
		KakaoJobSource source = new KakaoJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "jobList": [
				    {
				      "jobOfferId": 14276,
				      "jobOfferTitle": "Data Analytics Engineer (경력)",
				      "statusCode": "PROGRESS"
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).singleElement()
				.satisfies(posting -> {
					assertThat(posting.id()).isEqualTo("14276");
					assertThat(posting.companyDisplayName()).isEqualTo("\uCE74\uCE74\uC624");
					assertThat(posting.location()).isBlank();
				});
	}

	@Test
	void ignoresHiddenClosedAndIncompleteJobs() throws Exception {
		KakaoJobSource source = new KakaoJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "jobList": [
				    {
				      "realId": "P-1",
				      "jobOfferTitle": "Private Engineer",
				      "privateFlag": true,
				      "statusCode": "PROGRESS"
				    },
				    {
				      "realId": "P-2",
				      "jobOfferTitle": "Closed Engineer",
				      "closeFlag": true,
				      "statusCode": "PROGRESS"
				    },
				    {
				      "realId": "P-3",
				      "jobOfferTitle": "Ended Engineer",
				      "statusCode": "END"
				    },
				    {
				      "realId": "P-4",
				      "statusCode": "PROGRESS"
				    },
				    {
				      "jobOfferTitle": "Missing ID Engineer",
				      "statusCode": "PROGRESS"
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).isEmpty();
	}

	@Test
	void parsesTotalPageForPagedPartCollection() throws Exception {
		KakaoJobParser parser = new KakaoJobParser();

		int totalPage = parser.totalPage("""
				{
				  "jobList": [],
				  "totalPage": 2
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(totalPage).isEqualTo(2);
	}
}
