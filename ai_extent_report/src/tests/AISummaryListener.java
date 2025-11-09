package tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.AISummaryClient;
import ai.PromptBuilder;

public class AISummaryListener implements ITestListener {
	private final Queue<Map<String, Object>> failures = new ConcurrentLinkedQueue<>();
	private int passed = 0, failed = 0, skipped = 0;
	private long startTime;

	@Override
	public void onStart(ITestContext context) {
		startTime = System.currentTimeMillis();
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		passed++;
	}

	@Override
	public void onTestFailure(ITestResult result) {
		failed++;
		Map<String, Object> f = new HashMap<>();
		f.put("testName", result.getMethod().getMethodName());
		Throwable t = result.getThrowable();
		if (t != null) {
			f.put("error", t.getClass().getSimpleName());
			f.put("message", t.getMessage());
			String stack = Arrays.toString(Arrays.stream(t.getStackTrace()).limit(5).toArray());
			f.put("stackTrace", stack);
		}
		Object screenshot = result.getAttribute("screenshot");
		if (screenshot != null)
			f.put("screenshot", screenshot.toString());
		failures.add(f);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		skipped++;
	}

	@Override
	public void onFinish(ITestContext context) {
		long durationSecs = (System.currentTimeMillis() - startTime) / 1000;
		Map<String, Object> summary = new HashMap<>();
		summary.put("total", passed + failed + skipped);
		summary.put("passed", passed);
		summary.put("failed", failed);
		summary.put("skipped", skipped);
		summary.put("duration_secs", durationSecs);

		List<Map<String, Object>> failureList = new ArrayList<>(failures);

		String prompt = PromptBuilder.buildPrompt(summary, failureList);
		AISummaryClient client = new AISummaryClient();

		try {
			String aiResponse = client.requestSummary(prompt);
			System.out.println("=== AI Test Summary ===");
			System.out.println(aiResponse);

			// Write AI summary to HTML
			writeHtmlSummary(aiResponse, summary);

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Failed to call AI summarizer: " + e.getMessage());
		}
	}

	/** Writes a simple HTML report to target/ai-summary.html */
	private void writeHtmlSummary(String aiResponse, Map<String, Object> summary) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode json;
			try {
				json = mapper.readTree(aiResponse);
			} catch (Exception e) {
				// if LLM returned plain text, wrap it
				json = mapper.createObjectNode().put("raw_output", aiResponse);
			}

			File targetDir = new File("target");
			if (!targetDir.exists())
				targetDir.mkdirs();

			File htmlFile = new File(targetDir, "ai-summary.html");
			try (FileWriter writer = new FileWriter(htmlFile)) {
				writer.write("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
				writer.write("<title>AI Test Summary</title>");
				writer.write("<style>");
				writer.write("body{font-family:Arial,sans-serif;margin:40px;background:#f9f9f9;}");
				writer.write(
						".container{background:white;padding:30px;border-radius:10px;box-shadow:0 2px 5px rgba(0,0,0,0.1);}");
				writer.write("h1{color:#0078d4;} pre{background:#f0f0f0;padding:10px;border-radius:6px;}");
				writer.write("</style></head><body>");
				writer.write("<div class='container'>");
				writer.write("<h1>AI-Generated Test Summary</h1>");
				writer.write("<p><b>Total:</b> " + summary.get("total") + " | <b>Passed:</b> " + summary.get("passed")
						+ " | <b>Failed:</b> " + summary.get("failed") + " | <b>Skipped:</b> " + summary.get("skipped")
						+ " | <b>Duration:</b> " + summary.get("duration_secs") + "s</p>");
				writer.write("<hr/>");

				if (json.has("summary")) {
					writer.write("<h2>Summary</h2><p>" + json.get("summary").asText() + "</p>");
				}
				if (json.has("failure_breakdown")) {
					writer.write("<h2>Failure Breakdown</h2><pre>" + json.get("failure_breakdown").toPrettyString()
							+ "</pre>");
				}
				if (json.has("root_cause_hypotheses")) {
					writer.write("<h2>Possible Root Causes</h2><ul>");
					for (JsonNode n : json.get("root_cause_hypotheses"))
						writer.write("<li>" + n.asText() + "</li>");
					writer.write("</ul>");
				}
				if (json.has("fix_suggestions")) {
					writer.write("<h2>Suggested Fixes</h2><ul>");
					for (JsonNode n : json.get("fix_suggestions"))
						writer.write("<li>" + n.asText() + "</li>");
					writer.write("</ul>");
				}
				if (json.has("flaky_tests")) {
					writer.write("<h2>Flaky Tests</h2><ul>");
					for (JsonNode n : json.get("flaky_tests"))
						writer.write("<li>" + n.asText() + "</li>");
					writer.write("</ul>");
				}
				if (json.has("important_logs")) {
					writer.write(
							"<h2>Important Logs</h2><pre>" + json.get("important_logs").toPrettyString() + "</pre>");
				}
				if (json.has("raw_output")) {
					writer.write("<h2>Raw Model Output</h2><pre>" + json.get("raw_output").asText() + "</pre>");
				}

				writer.write("</div></body></html>");
			}

			System.out.println("✅ AI summary saved to: " + htmlFile.getAbsolutePath());

		} catch (Exception e) {
			System.err.println("Failed to write AI summary HTML: " + e.getMessage());
		}
	}

	// other ITestListener methods (no-op)
	@Override
	public void onTestStart(ITestResult result) {
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

	@Override
	public void onTestFailedWithTimeout(ITestResult result) {
		onTestFailure(result);
	}

}