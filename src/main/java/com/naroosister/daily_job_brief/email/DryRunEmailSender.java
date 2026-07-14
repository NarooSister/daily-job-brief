package com.naroosister.daily_job_brief.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "daily-job-brief.mail.enabled", havingValue = "false", matchIfMissing = true)
public class DryRunEmailSender implements EmailSender {

	private static final Logger log = LoggerFactory.getLogger(DryRunEmailSender.class);

	@Override
	public void send(EmailMessage message) {
		log.info("Dry-run email prepared: subject={}", message.subject());
		log.debug("Dry-run email body: {}", message.htmlBody());
	}
}
