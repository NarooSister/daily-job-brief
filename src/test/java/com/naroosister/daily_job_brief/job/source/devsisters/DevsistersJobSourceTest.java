package com.naroosister.daily_job_brief.job.source.devsisters;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class DevsistersJobSourceTest {

	@Test
	void parsesGreetingOpeningResponse() throws Exception {
		DevsistersJobSource source = new DevsistersJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "success": true,
				  "data": {
				    "page": 0,
				    "pageSize": 100,
				    "totalCount": 1,
				    "datas": [
				      {
				        "deploy": true,
				        "openingId": 224177,
				        "title": "[\uAE30\uC220\uBCF8\uBD80] SRE/DevOps Engineer",
				        "group": {"name": "\uB370\uBE0C\uC2DC\uC2A4\uD130\uC988"},
				        "openingJobPosition": {
				          "openingJobPositions": [
				            {
				              "workspacePlace": {
				                "location": "\uB370\uBE0C\uC2DC\uC2A4\uD130\uC988 HQ",
				                "place": "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uAC15\uB0A8\uAD6C"
				              }
				            }
				          ]
				        }
				      }
				    ],
				    "totalPage": 1,
				    "hasNext": false
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).containsExactly(new JobPosting(
				"224177",
				"DEVSISTERS",
				"\uB370\uBE0C\uC2DC\uC2A4\uD130\uC988",
				"[\uAE30\uC220\uBCF8\uBD80] SRE/DevOps Engineer",
				"https://careers.devsisters.com/ko/o/224177",
				"\uB370\uBE0C\uC2DC\uC2A4\uD130\uC988 HQ"
		));
	}

	@Test
	void ignoresUndeployedAndIncompleteOpenings() throws Exception {
		DevsistersJobSource source = new DevsistersJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "data": {
				    "datas": [
				      {"deploy": false, "openingId": 1, "title": "Hidden Engineer"},
				      {"deploy": true, "openingId": 2},
				      {"deploy": true, "title": "Missing ID Engineer"}
				    ]
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).isEmpty();
	}

	@Test
	void fallsBackToDefaultDisplayNameAndStreetAddress() throws Exception {
		DevsistersJobSource source = new DevsistersJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "data": {
				    "datas": [
				      {
				        "openingId": 3,
				        "title": "Platform Engineer",
				        "openingJobPosition": {
				          "openingJobPositions": [
				            {"workspacePlace": {"place": "\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uAC15\uB0A8\uAD6C"}}
				          ]
				        }
				      }
				    ]
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).singleElement().satisfies(posting -> {
			assertThat(posting.companyDisplayName()).isEqualTo("\uB370\uBE0C\uC2DC\uC2A4\uD130\uC988");
			assertThat(posting.location()).isEqualTo("\uC11C\uC6B8\uD2B9\uBCC4\uC2DC \uAC15\uB0A8\uAD6C");
		});
	}

	@Test
	void readsPageCountAndBuildsZeroBasedPageUri() throws Exception {
		DevsistersJobParser parser = new DevsistersJobParser();
		DevsistersJobSource source = new DevsistersJobSource(parser);

		int totalPage = parser.totalPage("""
				{"data":{"totalPage":3,"datas":[]}}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(totalPage).isEqualTo(3);
		assertThat(source.jobsUri(2).toString())
				.isEqualTo("https://api.greetinghr.com/ats/v1.1/career/workspaces/12227/openings?page=2&pageSize=100");
	}
}
