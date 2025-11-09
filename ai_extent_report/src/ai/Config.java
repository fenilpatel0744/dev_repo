package ai;

public class Config {

	// Ollama local API
	public static final String OLLAMA_BASE = System.getenv().getOrDefault("OLLAMA_BASE", "http://localhost:11434");
	// Model name to use on Ollama (can be "llama3.2", "llama3.2:3b", etc.)
	public static final String MODEL = System.getenv().getOrDefault("LLAMA_MODEL", "llama3.2:3b");
	// Keep stream false for simple requests
	public static final boolean STREAM = false;
}
