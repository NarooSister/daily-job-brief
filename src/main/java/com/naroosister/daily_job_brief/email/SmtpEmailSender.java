package com.naroosister.daily_job_brief.email;

import com.naroosister.daily_job_brief.config.DailyJobBriefProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "daily-job-brief.mail.enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

	private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

	private final JavaMailSender mailSender;
	private final String from;

	@Autowired
	public SmtpEmailSender(
			JavaMailSender mailSender,
			DailyJobBriefProperties properties,
			Environment environment
	) {
		this(mailSender, fromAddress(properties, environment));
	}

	SmtpEmailSender(JavaMailSender mailSender, String from) {
		this.mailSender = mailSender;
		this.from = from;
	}

	@Override
	public void send(EmailMessage message) {
		mailSender.send(mimeMessage -> {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
			helper.setFrom(from);
			helper.setTo(message.to());
			helper.setSubject(message.subject());
			helper.setText(message.htmlBody(), true);
		});
		log.info("Email sent: subject={}", message.subject());
	}

	private static String fromAddress(DailyJobBriefProperties properties, Environment environment) {
		String configuredFrom = properties.mail().from();
		if (configuredFrom != null) {
			return configuredFrom;
		}
		return environment.getProperty("spring.mail.username", "");
	}
}
