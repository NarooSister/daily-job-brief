package com.naroosister.daily_job_brief.job.source.coupang;

import com.naroosister.daily_job_brief.job.JobPosting;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class CoupangJobParser {

	private static final String COMPANY = "COUPANG";
	private static final String COMPANY_DISPLAY_NAME = "쿠팡";
	private static final URI JOBS_URI = URI.create("https://www.coupang.jobs/en/jobs/");
	private static final Pattern PAGE_QUERY = Pattern.compile("[?&]page=(\\d+)");
	private static final Pattern JOB_PATH = Pattern.compile("^/(?:[a-z]{2}/)?jobs?/(.+?)/?$");
	private static final Pattern LEADING_JOB_ID = Pattern.compile("^(\\d+)(?:/.*)?$");

	List<JobPosting> parse(byte[] html) throws IOException {
		Document document = Jsoup.parse(new ByteArrayInputStream(html), null, JOBS_URI.toString());
		return parse(document);
	}

	List<JobPosting> parse(String html) {
		return parse(Jsoup.parse(html, JOBS_URI.toString()));
	}

	int totalPage(byte[] html) throws IOException {
		Document document = Jsoup.parse(new ByteArrayInputStream(html), null, JOBS_URI.toString());
		return totalPage(document);
	}

	private List<JobPosting> parse(Document document) {
		Map<String, JobPosting> postings = new LinkedHashMap<>();

		for (Element link : document.select("a[href]")) {
			String url = link.absUrl("href");
			String id = jobId(url);
			String title = title(link.text());

			if (!id.isBlank() && !title.isBlank()) {
				postings.putIfAbsent(id, new JobPosting(
						id,
						COMPANY,
						COMPANY_DISPLAY_NAME,
						title,
						url,
						location(link)
				));
			}
		}

		return new ArrayList<>(postings.values());
	}

	private int totalPage(Document document) {
		int totalPage = 1;
		for (Element link : document.select("a[href]")) {
			Matcher matcher = PAGE_QUERY.matcher(link.attr("href"));
			if (matcher.find()) {
				totalPage = Math.max(totalPage, Integer.parseInt(matcher.group(1)));
			}
		}
		return totalPage;
	}

	private String jobId(String url) {
		URI uri = URI.create(url);
		if (!"www.coupang.jobs".equalsIgnoreCase(uri.getHost())) {
			return "";
		}
		if (uri.getRawFragment() != null) {
			return "";
		}

		Matcher matcher = JOB_PATH.matcher(uri.getPath());
		if (!matcher.matches()) {
			return "";
		}

		String pathId = matcher.group(1).trim();
		if (pathId.isBlank() || pathId.equals("saved-jobs")) {
			return "";
		}
		return queryParameter(uri.getRawQuery(), "gh_jid")
				.orElseGet(() -> pathJobId(pathId));
	}

	private java.util.Optional<String> queryParameter(String rawQuery, String name) {
		if (rawQuery == null || rawQuery.isBlank()) {
			return java.util.Optional.empty();
		}

		for (String parameter : rawQuery.split("&")) {
			int separator = parameter.indexOf('=');
			String parameterName = separator >= 0 ? parameter.substring(0, separator) : parameter;
			if (name.equals(parameterName)) {
				String value = separator >= 0 ? parameter.substring(separator + 1) : "";
				return java.util.Optional.of(URLDecoder.decode(value, StandardCharsets.UTF_8).trim())
						.filter(decoded -> !decoded.isBlank());
			}
		}
		return java.util.Optional.empty();
	}

	private String pathJobId(String pathId) {
		Matcher matcher = LEADING_JOB_ID.matcher(pathId);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return pathId;
	}

	private String title(String text) {
		return text
				.replaceAll("\\s+", " ")
				.trim();
	}

	private String location(Element titleLink) {
		Element container = titleLink.parent();
		for (int depth = 0; container != null && depth < 4; depth++) {
			String location = firstLocationText(container);
			if (!location.isBlank()) {
				return location;
			}
			container = container.parent();
		}
		return "";
	}

	private String firstLocationText(Element container) {
		for (Element candidate : container.select("li, span, div")) {
			String text = title(candidate.text());
			if (isLocationText(text)) {
				return text;
			}
		}
		return "";
	}

	private boolean isLocationText(String text) {
		return switch (text) {
			case "Beijing", "Bengaluru", "Changhua", "Chiba", "Hyderabad", "Mountain View",
					"Osaka", "Riverside", "Seattle", "Seoul", "Shanghai", "Singapore",
					"South Korea", "Tainan", "Taipei", "Taoyuan", "Tokyo" -> true;
			default -> false;
		};
	}
}
