package com.naroosister.daily_job_brief.email;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

class SmtpEmailSenderTest {

	@Test
	void buildsMimeMessageWithHtmlBody() throws Exception {
		CapturingJavaMailSender mailSender = new CapturingJavaMailSender();
		SmtpEmailSender sender = new SmtpEmailSender(mailSender, "sender@example.test");

		sender.send(new EmailMessage(
				"subscriber-a@example.test",
				"[daily-job-brief] New jobs: 1",
				"<html><body><strong>Hello</strong></body></html>"
		));

		MimeMessage message = mailSender.sentMessage;
		assertThat(message.getFrom()[0].toString()).isEqualTo("sender@example.test");
		assertThat(message.getAllRecipients()[0].toString()).isEqualTo("subscriber-a@example.test");
		assertThat(message.getSubject()).isEqualTo("[daily-job-brief] New jobs: 1");
		assertThat(message.getContent().toString()).contains("<strong>Hello</strong>");
	}

	private static class CapturingJavaMailSender implements JavaMailSender {

		private MimeMessage sentMessage;

		@Override
		public MimeMessage createMimeMessage() {
			return new MimeMessage(Session.getInstance(new Properties()));
		}

		@Override
		public MimeMessage createMimeMessage(InputStream contentStream) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void send(MimeMessage mimeMessage) {
			this.sentMessage = mimeMessage;
		}

		@Override
		public void send(MimeMessage... mimeMessages) {
			this.sentMessage = mimeMessages[0];
		}

		@Override
		public void send(MimeMessagePreparator mimeMessagePreparator) {
			MimeMessage mimeMessage = createMimeMessage();
			try {
				mimeMessagePreparator.prepare(mimeMessage);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			this.sentMessage = mimeMessage;
		}

		@Override
		public void send(MimeMessagePreparator... mimeMessagePreparators) {
			send(mimeMessagePreparators[0]);
		}

		@Override
		public void send(SimpleMailMessage simpleMessage) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void send(SimpleMailMessage... simpleMessages) {
			throw new UnsupportedOperationException();
		}
	}
}
