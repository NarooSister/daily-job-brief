package com.naroosister.daily_job_brief.job;

public record SourceExecutionReport(
		String company,
		boolean success,
		int fetchedCount,
		String errorType,
		String errorMessage,
		Exception exception
) {

	public static SourceExecutionReport success(String company, int fetchedCount) {
		return new SourceExecutionReport(company, true, fetchedCount, "", "", null);
	}

	public static SourceExecutionReport failure(String company, Exception exception) {
		return new SourceExecutionReport(
				company,
				false,
				0,
				exception.getClass().getName(),
				message(exception),
				exception
		);
	}

	public String errorSummary() {
		if (errorType.isBlank()) {
			return errorMessage;
		}
		if (errorMessage.isBlank()) {
			return errorType;
		}
		return errorType + ": " + errorMessage;
	}

	private static String message(Throwable throwable) {
		if (throwable == null) {
			return "";
		}
		if (throwable.getMessage() != null && !throwable.getMessage().isBlank()) {
			return throwable.getMessage();
		}
		return message(throwable.getCause());
	}
}
