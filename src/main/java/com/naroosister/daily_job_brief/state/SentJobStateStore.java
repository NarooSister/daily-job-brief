package com.naroosister.daily_job_brief.state;

import com.naroosister.daily_job_brief.config.DailyJobBriefProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class SentJobStateStore {

	private final ObjectMapper objectMapper;
	private final Path statePath;

	@Autowired
	public SentJobStateStore(ObjectMapper objectMapper, DailyJobBriefProperties properties) {
		this(objectMapper, properties.stateFile());
	}

	SentJobStateStore(ObjectMapper objectMapper, String statePath) {
		this(objectMapper, Path.of(statePath));
	}

	private SentJobStateStore(ObjectMapper objectMapper, Path statePath) {
		this.objectMapper = objectMapper;
		this.statePath = statePath;
	}

	public SentJobState load() throws IOException {
		return load(statePath);
	}

	public SentJobState load(Path path) throws IOException {
		if (Files.notExists(path)) {
			return SentJobState.empty();
		}
		return objectMapper.readValue(path.toFile(), SentJobState.class);
	}

	public void save(SentJobState state) throws IOException {
		save(statePath, state);
	}

	public void save(Path path, SentJobState state) throws IOException {
		Path parent = path.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		objectMapper.writeValue(path.toFile(), state);
	}
}
