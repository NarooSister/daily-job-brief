package com.naroosister.daily_job_brief.job;

import java.util.List;

public record JobCollectionResult(
		List<JobPosting> postings,
		List<SourceExecutionReport> reports
) {
}
