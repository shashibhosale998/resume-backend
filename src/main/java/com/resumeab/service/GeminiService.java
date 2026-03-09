package com.resumeab.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GeminiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    // ✅ Updated to accept jobDescription
    public String analyzeResume(String resumeText, String jobDescription) throws Exception {

        // ✅ Full ATS prompt with JD matching + formatting check
        String prompt = "You are an expert ATS (Applicant Tracking System) resume analyzer.\n\n"
                + "Analyze the resume below against the job description provided and give a COMPLETE ATS report with ALL of the following sections:\n\n"

                + "1. RESUME SCORE (out of 100)\n"
                + "   - Overall quality score\n\n"

                + "2. ATS VERDICT\n"
                + "   - PASS or FAIL\n"
                + "   - One line reason\n\n"

                + "3. JOB DESCRIPTION MATCH\n"
                + "   - Match percentage (e.g. 78%)\n"
                + "   - Keywords from JD found in resume\n"
                + "   - Keywords from JD missing in resume\n\n"

                + "4. DETECTED SKILLS\n"
                + "   - List all skills found in the resume\n\n"

                + "5. MISSING SKILLS\n"
                + "   - Skills missing compared to the job description\n\n"

                + "6. FORMATTING CHECK\n"
                + "   - Does resume have proper sections? (Summary, Experience, Education, Skills)\n"
                + "   - Are dates mentioned properly?\n"
                + "   - Are bullet points and action verbs used?\n"
                + "   - Is resume length appropriate?\n"
                + "   - Any ATS-unfriendly formatting issues?\n\n"

                + "7. SUGGESTIONS TO IMPROVE\n"
                + "   - Specific actionable tips to improve the resume\n\n"

                + "---\n"
                + "JOB DESCRIPTION:\n"
                + (jobDescription != null && !jobDescription.isBlank()
                    ? jobDescription
                    : "No job description provided. Do general ATS analysis.")
                + "\n\n"
                + "---\n"
                + "RESUME:\n"
                + resumeText;

        String safePrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        String requestBody = "{"
                + "\"model\": \"llama-3.3-70b-versatile\","
                + "\"max_tokens\": 2048,"
                + "\"messages\": ["
                + "  {"
                + "    \"role\": \"user\","
                + "    \"content\": \"" + safePrompt + "\""
                + "  }"
                + "]"
                + "}";

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(60000);

        OutputStream os = conn.getOutputStream();
        os.write(requestBody.getBytes("UTF-8"));
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();

        if (responseCode == 429) {
            conn.disconnect();
            throw new RuntimeException("QUOTA_EXCEEDED: Groq rate limit reached. Please try again.");
        }

        if (responseCode != 200) {
            InputStream errorStream = conn.getErrorStream();
            StringBuilder errorResponse = new StringBuilder();
            if (errorStream != null) {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
                String errorLine;
                while ((errorLine = errorReader.readLine()) != null) {
                    errorResponse.append(errorLine);
                }
                errorReader.close();
            }
            conn.disconnect();
            throw new RuntimeException("GROQ_API_ERROR [" + responseCode + "]: " + errorResponse.toString());
        }

        InputStream inputStream = conn.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();
        conn.disconnect();

        return response.toString();
    }
}