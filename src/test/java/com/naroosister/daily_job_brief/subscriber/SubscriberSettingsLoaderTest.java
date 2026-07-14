package com.naroosister.daily_job_brief.subscriber;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

class SubscriberSettingsLoaderTest {

	@TempDir
	Path tempDir;

	@Test
	void loadsSubscribersFromJsonFile() throws Exception {
		Path subscribersPath = tempDir.resolve("subscribers.json");
		Files.writeString(subscribersPath, """
				{
				  "subscribers": [
				    {
				      "id": "subscriber-a",
				      "email": "subscriber-a@example.test",
				      "keywords": ["DevOps", "SRE", "Platform Engineer"]
				    },
				    {
				      "id": "subscriber-b",
				      "email": "subscriber-b@example.test",
				      "keywords": ["Backend", "Java", "Spring"]
				    }
				  ]
				}
				""");

		SubscriberSettingsLoader loader = new SubscriberSettingsLoader(new ObjectMapper(), subscribersPath.toString());

		SubscriberSettings settings = loader.load();

		assertThat(settings.subscribers()).hasSize(2);
		assertThat(settings.subscribers().getFirst().id()).isEqualTo("subscriber-a");
		assertThat(settings.subscribers().getFirst().keywords()).containsExactly("DevOps", "SRE", "Platform Engineer");
	}
}
