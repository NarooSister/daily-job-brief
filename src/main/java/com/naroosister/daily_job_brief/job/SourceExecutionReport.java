package com.naroosister.daily_job_brief.job;

public record SourceExecutionReport(
		String company,
		boolean success,
		int fetchedCount,
		String errorMessage
) {

	public static SourceExecutionReport success(String company, int fetchedCount) {
		return new SourceExecutionReport(company, true, fetchedCount, "");
	}

	public static SourceExecutionReport failure(String company, Exception exception) {
		return new SourceExecutionReport(company, false, 0, exception.getMessage());
	}
}
