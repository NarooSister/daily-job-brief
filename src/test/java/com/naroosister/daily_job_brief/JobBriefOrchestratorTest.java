package com.naroosister.daily_job_brief;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.naroosister.daily_job_brief.config.DailyJobBriefProperties;
import com.naroosister.daily_job_brief.email.EmailContentBuilder;
import com.naroosister.daily_job_brief.email.EmailMessage;
import com.naroosister.daily_job_brief.email.EmailSender;
import com.naroosister.daily_job_brief.job.JobCollectionService;
import com.naroosister.daily_job_brief.job.JobPosting;
import com.naroosister.daily_job_brief.job.JobSource;
import com.naroosister.daily_job_brief.matching.JobMatcher;
import com.naroosister.daily_job_brief.state.SentJobState;
import com.naroosister.daily_job_brief.state.SentJobStateStore;
import com.naroosister.daily_job_brief.state.SentJobTracker;
import com.naroosister.daily_job_brief.subscriber.SubscriberSettingsLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

class JobBriefOrchestratorTest {

	@TempDir
	Path tempDir;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void sendsEmailAndSavesStateForNewMatches() throws Exception {
		Path subscribersPath = subscribers("""
				{
				  "subscribers": [
				    {
				      "id": "subscriber-a",
				      "email": "subscriber-a@example.test",
				      "keywords": ["Backend"]
				    }
				  ]
				}
				""");
		Path statePath = tempDir.resolve("sent-jobs.json");
		CapturingEmailSender emailSender = new CapturingEmailSender();

		orchestrator(subscribersPath, statePath, emailSender).run();

		SentJobState state = stateStore(statePath).load();
		assertThat(emailSender.messages).hasSize(1);
		assertThat(state.subscribers()).containsEntry("subscriber-a", List.of("DAANGN:1"));
	}

	@Test
	void savesStateForAlreadySentSubscriberWhenLaterEmailFails() throws Exception {
		Path subscribersPath = subscribers("""
				{
				  "subscribers": [
				    {
				      "id": "subscriber-a",
				      "email": "subscriber-a@example.test",
				      "keywords": ["Backend"]
				    },
				    {
				      "id": "subscriber-b",
				      "email": "subscriber-b@example.test",
				      "keywords": ["Backend"]
				    }
				  ]
				}
				""");
		Path statePath = tempDir.resolve("sent-jobs.json");
		FailingAfterFirstEmailSender emailSender = new FailingAfterFirstEmailSender();

		assertThatThrownBy(() -> orchestrator(subscribersPath, statePath, emailSender).run())
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("email failed");

		SentJobState state = stateStore(statePath).load();
		assertThat(emailSender.messages).hasSize(1);
		assertThat(state.subscribers()).containsEntry("subscriber-a", List.of("DAANGN:1"));
		assertThat(state.subscribers()).doesNotContainKey("subscriber-b");
	}

	private JobBriefOrchestrator orchestrator(
			Path subscribersPath,
			Path statePath,
			EmailSender emailSender
	) {
		DailyJobBriefProperties properties = properties(subscribersPath, statePath);
		return new JobBriefOrchestrator(
				new SubscriberSettingsLoader(objectMapper, properties),
				new JobCollectionService(List.of(new StaticJobSource()), properties),
				new JobMatcher(),
				stateStore(statePath),
				new SentJobTracker(),
				new EmailContentBuilder(),
				emailSender
		);
	}

	private SentJobStateStore stateStore(Path statePath) {
		return new SentJobStateStore(objectMapper, properties(tempDir.resolve("subscribers.json"), statePath));
	}

	private DailyJobBriefProperties properties(Path subscribersPath, Path statePath) {
		return new DailyJobBriefProperties(
				subscribersPath.toString(),
				statePath.toString(),
				null,
				new DailyJobBriefProperties.Sources(List.of(), List.of())
		);
	}

	private Path subscribers(String json) throws Exception {
		Path subscribersPath = tempDir.resolve("subscribers.json");
		Files.writeString(subscribersPath, json);
		return subscribersPath;
	}

	private static class CapturingEmailSender implements EmailSender {

		protected final List<EmailMessage> messages = new ArrayList<>();

		@Override
		public void send(EmailMessage message) {
			messages.add(message);
		}
	}

	private static class FailingAfterFirstEmailSender extends CapturingEmailSender {

		@Override
		public void send(EmailMessage message) {
			if (!messages.isEmpty()) {
				throw new IllegalStateException("email failed");
			}
			super.send(message);
		}
	}

	private static class StaticJobSource implements JobSource {

		@Override
		public String company() {
			return "DAANGN";
		}

		@Override
		public List<JobPosting> fetch() {
			return List.of(new JobPosting(
					"1",
					"DAANGN",
					"당근",
					"Backend Engineer",
					"https://example.com/jobs/1",
					"Korea"
			));
		}
	}
}
