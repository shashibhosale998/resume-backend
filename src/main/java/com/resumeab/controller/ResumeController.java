package com.resumeab.controller;

import com.resumeab.model.ResumeResponse;
import com.resumeab.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = {
	    "http://localhost:5173",
	    "https://resume-analyzer-frontend-two.vercel.app"
	})
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "jobDescription", defaultValue = "") String jobDescription  // ✅ NEW
    ) {
        try {
            ResumeResponse result = resumeService.analyzeResume(file, jobDescription);  // ✅ pass it
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {

            Map<String, String> errorBody = new HashMap<>();
            String msg = e.getMessage();

            if (msg != null && msg.startsWith("QUOTA_EXCEEDED")) {
                errorBody.put("error", "quota_exceeded");
                errorBody.put("message", "⚠️ AI quota exceeded. Please try again later.");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorBody);

            } else if (msg != null && (msg.startsWith("GROQ_API_ERROR") || msg.startsWith("GEMINI_API_ERROR"))) {
                errorBody.put("error", "api_error");
                errorBody.put("message", "❌ AI service error: " + msg);
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody);

            } else {
                errorBody.put("error", "server_error");
                errorBody.put("message", "❌ Something went wrong: " + (msg != null ? msg : "Unknown error"));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
            }

        } catch (Exception e) {
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", "server_error");
            errorBody.put("message", "❌ Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
        }
    }
}