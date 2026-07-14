package com.naroosister.daily_job_brief.job;

import com.naroosister.daily_job_brief.config.DailyJobBriefProperties;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class JobCollectionService {

	private final List<JobSource> jobSources;
	private final DailyJobBriefProperties properties;

	public JobCollectionService(List<JobSource> jobSources, DailyJobBriefProperties properties) {
		this.jobSources = jobSources;
		this.properties = properties;
	}

	public JobCollectionResult collect() {
		Map<String, JobPosting> postingsByKey = new LinkedHashMap<>();
		List<SourceExecutionReport> reports = new ArrayList<>();

		for (JobSource jobSource : enabledSources()) {
			if (!collect(jobSource, postingsByKey, reports)) {
				break;
			}
		}

		return new JobCollectionResult(
				List.copyOf(postingsByKey.values()),
				List.copyOf(reports)
		);
	}

	private List<JobSource> enabledSources() {
		return jobSources.stream()
				.filter(jobSource -> properties.sources().isEnabled(jobSource.company()))
				.toList();
	}

	private boolean collect(
			JobSource jobSource,
			Map<String, JobPosting> postingsByKey,
			List<SourceExecutionReport> reports
	) {
		try {
			List<JobPosting> postings = jobSource.fetch();
			postings.forEach(posting -> postingsByKey.putIfAbsent(jobKey(posting), posting));
			reports.add(SourceExecutionReport.success(jobSource.company(), postings.size()));
			return true;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			reports.add(SourceExecutionReport.failure(jobSource.company(), exception));
			return false;
		} catch (IOException | RuntimeException exception) {
			reports.add(SourceExecutionReport.failure(jobSource.company(), exception));
			return true;
		}
	}

	private String jobKey(JobPosting posting) {
		return posting.company() + ":" + posting.id();
	}
}
