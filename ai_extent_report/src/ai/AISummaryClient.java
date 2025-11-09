package ai;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AISummaryClient {
	private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

	// Increase timeouts (2 minutes read, 30 sec connect)
	private static final OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(Duration.ofSeconds(30))
			.readTimeout(Duration.ofSeconds(120)).writeTimeout(Duration.ofSeconds(120))
			.callTimeout(Duration.ofSeconds(180)).build();

	public String requestSummary(String prompt) throws IOException {
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("model", "llama3.2:3b");
		requestBody.put("prompt", prompt);
		requestBody.put("stream", false);

		ObjectMapper mapper = new ObjectMapper();
		String json = mapper.writeValueAsString(requestBody);

		RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
		Request request = new Request.Builder().url(OLLAMA_URL).post(body).build();

		System.out.println("🚀 Sending to Ollama:\n" + json);

		// Retry up to 3 times
		IOException lastError = null;
		for (int attempt = 1; attempt <= 3; attempt++) {
			try (Response response = httpClient.newCall(request).execute()) {
				if (!response.isSuccessful()) {
					String errBody = response.body() != null ? response.body().string() : "no body";
					throw new IOException("Unexpected response: " + response + "\nBody: " + errBody);
				}

				String resp = response.body().string();
				System.out.println("✅ Ollama response received (Attempt " + attempt + ")");
				return extractResponse(resp);
			} catch (IOException e) {
				lastError = e;
				System.err.println("⚠️ Attempt " + attempt + " failed: " + e.getMessage());
				try {
					Thread.sleep(3000L * attempt); // exponential backoff
				} catch (InterruptedException ignored) {
				}
			}
		}
		throw new IOException("Failed to get AI summary after retries", lastError);
	}

	private String extractResponse(String json) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<?, ?> obj = mapper.readValue(json, Map.class);
			Object resp = obj.get("response");
			return resp != null ? resp.toString().trim() : json;
		} catch (Exception e) {
			return json;
		}
	}

}
