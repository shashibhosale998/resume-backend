package com.resumeab.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Value("${groq.api.key}")   // ✅ changed from gemini.api.key
    private String apiKey;

    @Value("${groq.api.url}")   // ✅ changed from gemini.api.url
    private String apiUrl;

    public String getApiKey() {
        return apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }
}