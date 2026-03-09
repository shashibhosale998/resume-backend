package com.resumeab.service;

import com.resumeab.model.ResumeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private GeminiService geminiService;

    // ✅ Updated to accept jobDescription
    public ResumeResponse analyzeResume(MultipartFile file, String jobDescription) throws Exception {
        String resumeText = pdfService.extractText(file);
        String aiResult = geminiService.analyzeResume(resumeText, jobDescription);  // ✅ pass it

        ResumeResponse response = new ResumeResponse();
        response.setResult(aiResult);
        return response;
    }
}