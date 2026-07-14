package com.naroosister.daily_job_brief.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class BaeminJobSourceTest {

	@Test
	void parsesBaeminRecruitListApiResponse() throws Exception {
		BaeminJobSource source = new BaeminJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "code": "2000",
				  "message": "OK",
				  "data": {
				    "pageSize": 100,
				    "pageNumber": 1,
				    "totalPageNumber": 1,
				    "totalSize": 1,
				    "list": [
				      {
				        "recruitNumber": "R2606023",
				        "recruitName": "Server(\uBC30\uCC28\uC2DC\uC2A4\uD15C)",
				        "recruitDeleteYn": false,
				        "isHidden": false,
				        "isAfterOrEqualOpenDay": true,
				        "isAfterOrEqualEndDay": false,
				        "isTemporaryStatus": false
				      }
				    ]
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).containsExactly(new JobPosting(
				"R2606023",
				"BAEMIN",
				"\uC6B0\uC544\uD55C\uD615\uC81C\uB4E4",
				"Server(\uBC30\uCC28\uC2DC\uC2A4\uD15C)",
				"https://career.woowahan.com/recruitment/R2606023/detail",
				""
		));
	}

	@Test
	void ignoresHiddenDeletedClosedTemporaryAndIncompleteJobs() throws Exception {
		BaeminJobSource source = new BaeminJobSource();

		List<JobPosting> postings = source.parse("""
				{
				  "data": {
				    "list": [
				      {
				        "recruitNumber": "R1",
				        "recruitName": "Deleted Engineer",
				        "recruitDeleteYn": true
				      },
				      {
				        "recruitNumber": "R2",
				        "recruitName": "Hidden Engineer",
				        "isHidden": true
				      },
				      {
				        "recruitNumber": "R3",
				        "recruitName": "Closed Engineer",
				        "isAfterOrEqualEndDay": true
				      },
				      {
				        "recruitNumber": "R4",
				        "recruitName": "Temporary Engineer",
				        "isTemporaryStatus": true
				      },
				      {
				        "recruitNumber": "R5",
				        "recruitName": "Not Open Engineer",
				        "isAfterOrEqualOpenDay": false
				      },
				      {
				        "recruitNumber": "R6"
				      },
				      {
				        "recruitName": "Missing ID Engineer"
				      }
				    ]
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(postings).isEmpty();
	}

	@Test
	void parsesTotalPageForPagedCollection() throws Exception {
		BaeminJobParser parser = new BaeminJobParser();

		int totalPage = parser.totalPage("""
				{
				  "data": {
				    "list": [],
				    "totalPageNumber": 3
				  }
				}
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(totalPage).isEqualTo(3);
	}
}
