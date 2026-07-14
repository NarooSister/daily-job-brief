package com.naroosister.daily_job_brief.job.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Test;

class JobHttpClientTest {

	@Test
	void returnsResponseBodyForSuccessfulRequests() throws Exception {
		StubHttpClient stub = StubHttpClient.ok("[]".getBytes());
		JobHttpClient client = new JobHttpClient(stub);

		byte[] body = client.get(URI.create("https://example.com/jobs"), "EXAMPLE", JobHttpClient.ACCEPT_JSON);

		assertThat(body).containsExactly("[]".getBytes());
		assertThat(stub.lastRequest.headers().firstValue("Accept")).contains(JobHttpClient.ACCEPT_JSON);
		assertThat(stub.lastRequest.timeout()).contains(Duration.ofSeconds(20));
	}

	@Test
	void throwsIOExceptionForUnsuccessfulRequests() {
		JobHttpClient client = new JobHttpClient(StubHttpClient.status(503));

		assertThatThrownBy(() -> client.get(URI.create("https://example.com/jobs"), "EXAMPLE", JobHttpClient.ACCEPT_JSON))
				.isInstanceOf(IOException.class)
				.hasMessage("Failed to fetch EXAMPLE jobs. status=503");
	}

	private static class StubHttpClient extends HttpClient {

		private final int statusCode;
		private final byte[] body;
		private HttpRequest lastRequest;

		private StubHttpClient(int statusCode, byte[] body) {
			this.statusCode = statusCode;
			this.body = body;
		}

		static StubHttpClient ok(byte[] body) {
			return new StubHttpClient(200, body);
		}

		static StubHttpClient status(int statusCode) {
			return new StubHttpClient(statusCode, new byte[0]);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
			this.lastRequest = request;
			return new StubHttpResponse<>(statusCode, (T) body);
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync(
				HttpRequest request,
				HttpResponse.BodyHandler<T> responseBodyHandler
		) {
			return CompletableFuture.completedFuture(send(request, responseBodyHandler));
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync(
				HttpRequest request,
				HttpResponse.BodyHandler<T> responseBodyHandler,
				HttpResponse.PushPromiseHandler<T> pushPromiseHandler
		) {
			return CompletableFuture.completedFuture(send(request, responseBodyHandler));
		}

		@Override
		public Optional<CookieHandler> cookieHandler() {
			return Optional.empty();
		}

		@Override
		public Optional<Duration> connectTimeout() {
			return Optional.empty();
		}

		@Override
		public Redirect followRedirects() {
			return Redirect.NEVER;
		}

		@Override
		public Optional<ProxySelector> proxy() {
			return Optional.empty();
		}

		@Override
		public SSLContext sslContext() {
			return null;
		}

		@Override
		public SSLParameters sslParameters() {
			return null;
		}

		@Override
		public Optional<Authenticator> authenticator() {
			return Optional.empty();
		}

		@Override
		public Version version() {
			return Version.HTTP_2;
		}

		@Override
		public Optional<Executor> executor() {
			return Optional.empty();
		}
	}

	private record StubHttpResponse<T>(int statusCode, T body) implements HttpResponse<T> {

		@Override
		public HttpRequest request() {
			return null;
		}

		@Override
		public Optional<HttpResponse<T>> previousResponse() {
			return Optional.empty();
		}

		@Override
		public HttpHeaders headers() {
			return HttpHeaders.of(java.util.Map.of(), (name, value) -> true);
		}

		@Override
		public URI uri() {
			return URI.create("https://example.com/jobs");
		}

		@Override
		public HttpClient.Version version() {
			return HttpClient.Version.HTTP_2;
		}

		@Override
		public Optional<SSLSession> sslSession() {
			return Optional.empty();
		}
	}
}
