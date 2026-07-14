package com.naroosister.daily_job_brief;

import com.naroosister.daily_job_brief.email.EmailContentBuilder;
import com.naroosister.daily_job_brief.email.EmailMessage;
import com.naroosister.daily_job_brief.email.EmailSender;
import com.naroosister.daily_job_brief.job.JobCollectionResult;
import com.naroosister.daily_job_brief.job.JobCollectionService;
import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.SourceExecutionReport;
import com.naroosister.daily_job_brief.matching.JobMatcher;
import com.naroosister.daily_job_brief.state.SentJobState;
import com.naroosister.daily_job_brief.state.SentJobStateStore;
import com.naroosister.daily_job_brief.state.SentJobTracker;
import com.naroosister.daily_job_brief.subscriber.Subscriber;
import com.naroosister.daily_job_brief.subscriber.SubscriberSettings;
import com.naroosister.daily_job_brief.subscriber.SubscriberSettingsLoader;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JobBriefOrchestrator {

	private static final Logger log = LoggerFactory.getLogger(JobBriefOrchestrator.class);

	private final SubscriberSettingsLoader subscriberSettingsLoader;
	private final JobCollectionService jobCollectionService;
	private final JobMatcher jobMatcher;
	private final SentJobStateStore stateStore;
	private final SentJobTracker sentJobTracker;
	private final EmailContentBuilder emailContentBuilder;
	private final EmailSender emailSender;

	public JobBriefOrchestrator(
			SubscriberSettingsLoader subscriberSettingsLoader,
			JobCollectionService jobCollectionService,
			JobMatcher jobMatcher,
			SentJobStateStore stateStore,
			SentJobTracker sentJobTracker,
			EmailContentBuilder emailContentBuilder,
			EmailSender emailSender
	) {
		this.subscriberSettingsLoader = subscriberSettingsLoader;
		this.jobCollectionService = jobCollectionService;
		this.jobMatcher = jobMatcher;
		this.stateStore = stateStore;
		this.sentJobTracker = sentJobTracker;
		this.emailContentBuilder = emailContentBuilder;
		this.emailSender = emailSender;
	}

	public void run() throws IOException {
		log.info("Daily job brief started");

		SubscriberSettings settings = subscriberSettingsLoader.load();
		SentJobState state = stateStore.load();
		log.info("Loaded subscribers: {}", settings.subscribers().size());

		JobCollectionResult collectionResult = jobCollectionService.collect();
		logCollectionReports(collectionResult.reports());
		state = notifySubscribers(settings, state, collectionResult.postings());

		stateStore.save(state);
		log.info("Daily job brief finished");
	}

	private void logCollectionReports(List<SourceExecutionReport> reports) {
		for (SourceExecutionReport report : reports) {
			if (report.success()) {
				log.info("Fetched jobs: company={}, count={}", report.company(), report.fetchedCount());
			} else {
				log.warn("Failed to fetch jobs: company={}, error={}", report.company(), report.errorMessage());
			}
		}
	}

	private SentJobState notifySubscribers(
			SubscriberSettings settings,
			SentJobState state,
			List<JobPosting> allPostings
	) {
		SentJobState updatedState = state;
		for (Subscriber subscriber : settings.subscribers()) {
			List<JobPosting> matches = jobMatcher.match(subscriber, allPostings);
			List<JobPosting> newMatches = sentJobTracker.withoutAlreadySent(updatedState, subscriber, matches);
			logMatches(subscriber, matches, newMatches);

			if (!newMatches.isEmpty()) {
				EmailMessage message = emailContentBuilder.build(subscriber, newMatches);
				emailSender.send(message);
				updatedState = sentJobTracker.markSent(updatedState, subscriber, newMatches);
			}
		}
		return updatedState;
	}

	private void logMatches(
			Subscriber subscriber,
			List<JobPosting> matches,
			List<JobPosting> newMatches
	) {
		log.info("Subscriber processed: subscriberId={}, matched={}, new={}",
				subscriber.id(),
				matches.size(),
				newMatches.size());
		newMatches.forEach(posting -> log.debug(
				"New job: subscriberId={}, company={}, title={}, location={}, url={}",
				subscriber.id(),
				posting.company(),
				posting.title(),
				posting.location(),
				posting.url()));
	}
}
