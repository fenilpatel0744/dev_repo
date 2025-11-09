package ai;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PromptBuilder {

	public static String buildPrompt(Map<String, Object> summary, List<Map<String, Object>> failures) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			String summaryJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(summary);
			String failureJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(failures);

			return """
					You are an expert QA assistant analyzing automated Selenium Web Test results.

					Here are the test run statistics:
					%s

					Here are the details of failed tests:
					%s

					Please generate a professional, human-readable summary including:
					- 🧩 A short summary of total tests, pass/fail rate, and execution time
					- 🧠 Key insights on what likely caused the failures (root cause analysis)
					- 🛠 Suggested next actions or fixes
					- ⚡ Which tests might be flaky or unstable (if applicable)
					- 📈 Any patterns across failures (e.g., same module, timeout, locator issues)

					Respond in a concise **Markdown format**, not JSON.
					""".formatted(summaryJson, failureJson);
		} catch (Exception e) {
			return "Error building AI prompt: " + e.getMessage();
		}
	}
}
