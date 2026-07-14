package com.naroosister.daily_job_brief.subscriber;

import com.naroosister.daily_job_brief.config.DailyJobBriefProperties;
import java.io.IOException;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SubscriberSettingsLoader {

	private final ObjectMapper objectMapper;
	private final Path subscribersPath;

	@Autowired
	public SubscriberSettingsLoader(ObjectMapper objectMapper, DailyJobBriefProperties properties) {
		this(objectMapper, properties.subscribersFile());
	}

	SubscriberSettingsLoader(ObjectMapper objectMapper, String subscribersPath) {
		this(objectMapper, Path.of(subscribersPath));
	}

	private SubscriberSettingsLoader(ObjectMapper objectMapper, Path subscribersPath) {
		this.objectMapper = objectMapper;
		this.subscribersPath = subscribersPath;
	}

	public SubscriberSettings load() throws IOException {
		return load(subscribersPath);
	}

	public SubscriberSettings load(Path path) throws IOException {
		return objectMapper.readValue(path.toFile(), SubscriberSettings.class);
	}
}
