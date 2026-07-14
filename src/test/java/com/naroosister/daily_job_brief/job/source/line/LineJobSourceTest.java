package com.naroosister.daily_job_brief.job.source.line;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class LineJobSourceTest {

	@Test
	void parsesLineJobsPageDataResponse() throws Exception {
		LineJobSource source = new LineJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "result": {
				    "data": {
				      "allStrapiJobs": {
				        "edges": [
				          {
				            "node": {
				              "publish": true,
				              "strapiId": 3007,
				              "title": "LINE Pay Server Engineer",
				              "title_en": "LINE Pay Server Engineer",
				              "is_public": true,
				              "companies": [
				                {
				                  "name": "LINE Pay Plus",
				                  "id": 34
				                }
				              ],
				              "cities": [
				                {
				                  "name": "Bundang",
				                  "id": 2
				                }
				              ]
				            }
				          }
				        ]
				      }
				    }
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).containsExactly(new JobPosting(
				"3007",
				"LINE",
				"LINE Pay Plus",
				"LINE Pay Server Engineer",
				"https://careers.linecorp.com/ko/jobs/3007",
				"Bundang"
		));
	}

	@Test
	void fallsBackToEnglishTitleAndDefaultCompanyDisplayName() throws Exception {
		LineJobSource source = new LineJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "result": {
				    "data": {
				      "allStrapiJobs": {
				        "edges": [
				          {
				            "node": {
				              "publish": true,
				              "strapiId": 2993,
				              "title": "",
				              "title_en": "AI/Data Platform Engineer",
				              "is_public": true,
				              "companies": [],
				              "cities": []
				            }
				          }
				        ]
				      }
				    }
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).singleElement()
				.satisfies(posting -> {
					assertThat(posting.title()).isEqualTo("AI/Data Platform Engineer");
					assertThat(posting.companyDisplayName()).isEqualTo("LINE");
					assertThat(posting.location()).isBlank();
				});
	}

	@Test
	void ignoresHiddenJobsAndJobsWithoutRequiredFields() throws Exception {
		LineJobSource source = new LineJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "result": {
				    "data": {
				      "allStrapiJobs": {
				        "edges": [
				          {
				            "node": {
				              "publish": false,
				              "strapiId": 3007,
				              "title": "Hidden Engineer",
				              "is_public": true
				            }
				          },
				          {
				            "node": {
				              "publish": true,
				              "title": "Missing ID Engineer",
				              "is_public": true
				            }
				          },
				          {
				            "node": {
				              "publish": true,
				              "strapiId": 2993,
				              "is_public": true
				            }
				          }
				        ]
				      }
				    }
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).isEmpty();
	}
}
