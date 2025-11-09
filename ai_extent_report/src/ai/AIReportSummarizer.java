package ai;

import java.util.Map;

public class AIReportSummarizer {
	public static void main(String[] args) throws Exception {
		String reportPath = "test-output/emailable-report.html"; // adjust to your file
		Map<String, Object> reportData = HtmlReportParser.parseTestReport(reportPath);

		String prompt = buildPrompt(reportData);

		AISummaryClient client = new AISummaryClient();
		String aiSummary = client.requestSummary(prompt);

		System.out.println("\n🧾 AI-Generated Test Summary:\n");
		System.out.println(aiSummary);
	}

	private static String buildPrompt(Map<String, Object> data) {
		return """
				You are an expert QA analyst summarizing automated test results.

				Here are the extracted test statistics:
				""" + data + """

				Please generate a professional, human-readable summary including:
				- ✅ Total tests and success rate
				- ❌ Key failures and likely causes
				- 🧩 Any recurring patterns or modules affected
				- 💡 Suggested next actions or improvements
				- ⏱️ Overall execution performance and timing insights

				Respond concisely in **Markdown format**.
				""";
	}

}
