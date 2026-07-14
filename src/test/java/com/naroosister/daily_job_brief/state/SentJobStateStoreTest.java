package com.naroosister.daily_job_brief.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

class SentJobStateStoreTest {

	@TempDir
	Path tempDir;

	private final SentJobStateStore stateStore = new SentJobStateStore(new ObjectMapper(), "state/sent-jobs.json");

	@Test
	void loadsEmptyStateWhenFileDoesNotExist() throws Exception {
		SentJobState state = stateStore.load(tempDir.resolve("missing.json"));

		assertThat(state.subscribers()).isEmpty();
	}

	@Test
	void savesAndLoadsSentJobState() throws Exception {
		Path statePath = tempDir.resolve("sent-jobs.json");
		SentJobState state = new SentJobState(
				java.util.Map.of("subscriber-a", List.of("DAANGN:6615071003"))
		);

		stateStore.save(statePath, state);
		SentJobState loaded = stateStore.load(statePath);

		assertThat(loaded.subscribers()).containsEntry("subscriber-a", List.of("DAANGN:6615071003"));
		assertThat(Files.exists(statePath)).isTrue();
	}
}
