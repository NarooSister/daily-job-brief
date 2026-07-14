package com.naroosister.daily_job_brief.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class TossJobSourceTest {

	@Test
	void parsesTossJobsApiResponse() throws Exception {
		TossJobSource source = new TossJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "resultType": "SUCCESS",
				  "success": [
				    {
				      "id": 7797504003,
				      "title": "Backend Developer",
				      "absolute_url": "https://toss.im/career/job-detail?gh_jid=7797504003",
				      "location": {
				        "name": "Seoul"
				      },
				      "metadata": [
				        {
				          "id": 4169410003,
				          "name": "소속 회사",
				          "value": "토스뱅크"
				        }
				      ]
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).containsExactly(new JobPosting(
				"7797504003",
				"TOSS_BANK",
				"토스뱅크",
				"Backend Developer",
				"https://toss.im/career/job-detail?gh_jid=7797504003",
				""
		));
	}

	@Test
	void fallsBackToJobDetailUrlWhenAbsoluteUrlIsMissing() throws Exception {
		TossJobSource source = new TossJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "success": [
				    {
				      "id": 7746633003,
				      "title": "Platform Product Owner (Global)",
				      "location": {
				        "name": "Seoul"
				      },
				      "metadata": [
				        {
				          "id": 4169410003,
				          "name": "소속 회사",
				          "value": "토스"
				        }
				      ]
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).singleElement()
				.satisfies(posting -> {
					assertThat(posting.id()).isEqualTo("7746633003");
					assertThat(posting.url()).isEqualTo("https://toss.im/career/job-detail?gh_jid=7746633003");
				});
	}

	@Test
	void fallsBackToTossWhenAffiliateMetadataIsMissing() throws Exception {
		TossJobSource source = new TossJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "success": [
				    {
				      "id": 7601721003,
				      "title": "Direct Sales Assistant"
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).singleElement()
				.satisfies(posting -> {
					assertThat(posting.company()).isEqualTo("TOSS");
					assertThat(posting.companyDisplayName()).isEqualTo("토스");
				});
	}

	@Test
	void ignoresJobsWithoutRequiredFields() throws Exception {
		TossJobSource source = new TossJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "success": [
				    {
				      "id": 7601721003,
				      "location": {
				        "name": "Seoul"
				      }
				    },
				    {
				      "title": "Direct Sales Assistant"
				    }
				  ]
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).isEmpty();
	}
}
