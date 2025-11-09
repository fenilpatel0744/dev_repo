package ai;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlReportParser {

	public static Map<String, Object> parseTestReport(String reportPath) throws Exception {
		File input = new File(reportPath);
		Document doc = Jsoup.parse(input, "UTF-8");

		Map<String, Object> summary = new LinkedHashMap<>();

		// Extract table rows
		Element summaryTable = doc.selectFirst("table");
		if (summaryTable != null) {
			List<Element> rows = summaryTable.select("tr");
			for (Element row : rows) {
				if (row.text().contains("Total Tests")) {
					summary.put("total", extractNumber(row.text()));
				} else if (row.text().contains("Passed")) {
					summary.put("passed", extractNumber(row.text()));
				} else if (row.text().contains("Failed")) {
					summary.put("failed", extractNumber(row.text()));
				} else if (row.text().contains("Skipped")) {
					summary.put("skipped", extractNumber(row.text()));
				}
			}
		}

		// Optional: Extract failed test names
		List<String> failedTests = new ArrayList<>();
		for (Element tr : doc.select("tr")) {
			if (tr.text().toLowerCase().contains("failed")) {
				failedTests.add(tr.text());
			}
		}
		summary.put("failed_tests", failedTests);

		return summary;
	}

	private static int extractNumber(String text) {
		String digits = text.replaceAll("\\D+", "");
		return digits.isEmpty() ? 0 : Integer.parseInt(digits);
	}

}
