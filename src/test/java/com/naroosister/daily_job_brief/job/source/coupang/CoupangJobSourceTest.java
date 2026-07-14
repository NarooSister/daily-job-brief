package com.naroosister.daily_job_brief.job.source.coupang;

import static org.assertj.core.api.Assertions.assertThat;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class CoupangJobSourceTest {

	@Test
	void parsesCoupangJobListPageFromString() {
		CoupangJobSource source = new CoupangJobSource();

		List<JobPosting> postings = source.parse("""
				<html>
				  <body>
				    <article>
				      <h2>
				        <a href="/en/jobs/8063690/staff-back-end-engineer-new-fintech-product/?gh_jid=8063690">
				          Staff Back-end Engineer (New Fintech Product)
				        </a>
				      </h2>
				      <button>Save Saved</button>
				      <ul>
				        <li>Seoul</li>
				      </ul>
				    </article>
				    <nav>
				      <a href="/en/jobs/">Browse jobs</a>
				      <a href="/en/jobs/?page=2">2</a>
				    </nav>
				  </body>
				</html>
				""");

		assertThat(postings).containsExactly(new JobPosting(
				"8063690",
				"COUPANG",
				"쿠팡",
				"Staff Back-end Engineer (New Fintech Product)",
				"https://www.coupang.jobs/en/jobs/8063690/staff-back-end-engineer-new-fintech-product/?gh_jid=8063690",
				"Seoul"
		));
	}

	@Test
	void parsesCoupangJobListPageFromBytes() throws Exception {
		CoupangJobSource source = new CoupangJobSource();
		byte[] html = """
				<html>
				  <body>
				    <a href="/en/jobs/8052967/senior-security-engineer-digital-forensics-ediscovery/?gh_jid=8052967">
				      Senior Security Engineer (Digital Forensics &amp; eDiscovery)
				    </a>
				  </body>
				</html>
				""".getBytes(StandardCharsets.UTF_8);

		List<JobPosting> postings = source.parse(html);

		assertThat(postings).singleElement()
				.satisfies(posting -> {
					assertThat(posting.id()).isEqualTo("8052967");
					assertThat(posting.company()).isEqualTo("COUPANG");
					assertThat(posting.title()).isEqualTo("Senior Security Engineer (Digital Forensics & eDiscovery)");
				});
	}

	@Test
	void parsesSingularJobDetailPath() {
		CoupangJobSource source = new CoupangJobSource();

		List<JobPosting> postings = source.parse("""
				<html>
				  <body>
				    <a href="/en/job/seoul/senior-staff-backend-engineer/12345/67890">
				      Senior Staff Backend Engineer
				    </a>
				  </body>
				</html>
				""");

		assertThat(postings).singleElement()
				.satisfies(posting -> {
					assertThat(posting.id()).isEqualTo("seoul/senior-staff-backend-engineer/12345/67890");
					assertThat(posting.url()).isEqualTo("https://www.coupang.jobs/en/job/seoul/senior-staff-backend-engineer/12345/67890");
				});
	}

	@Test
	void fallsBackToLeadingNumericPathIdWhenGreenhouseIdIsMissing() {
		CoupangJobSource source = new CoupangJobSource();

		List<JobPosting> postings = source.parse("""
				<html>
				  <body>
				    <a href="/en/jobs/8063690/staff-back-end-engineer-new-fintech-product/">
				      Staff Back-end Engineer (New Fintech Product)
				    </a>
				  </body>
				</html>
				""");

		assertThat(postings).singleElement()
				.satisfies(posting -> assertThat(posting.id()).isEqualTo("8063690"));
	}

	@Test
	void ignoresPaginationAndNonCoupangLinks() {
		CoupangJobSource source = new CoupangJobSource();

		List<JobPosting> postings = source.parse("""
				<html>
				  <body>
				    <a href="/en/jobs/">Browse jobs</a>
				    <a href="/en/jobs/?page=2">2</a>
				    <a href="https://aboutcoupang.com">About Coupang</a>
				  </body>
				</html>
				""");

		assertThat(postings).isEmpty();
	}

	@Test
	void parsesTotalPageFromPaginationLinks() throws Exception {
		CoupangJobParser parser = new CoupangJobParser();

		int totalPage = parser.totalPage("""
				<html>
				  <body>
				    <a href="/en/jobs/?page=2">2</a>
				    <a href="/en/jobs/?page=33">33</a>
				  </body>
				</html>
				""".getBytes(StandardCharsets.UTF_8));

		assertThat(totalPage).isEqualTo(33);
	}
}
