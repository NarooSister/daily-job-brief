package com.naroosister.daily_job_brief.job;

import java.io.IOException;
import java.util.List;

public interface JobSource {

	String company();

	List<JobPosting> fetch() throws IOException, InterruptedException;
}
