package com.example.meetingminutesgenerator;

import org.springframework.stereotype.Service;

@Service
public class MinutesService {

    private final GeminiService geminiService;

    public MinutesService(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    public String generateSummary(String transcript) {
        return geminiService.generateSummary(transcript);
    }

    public String generateActionItems() {
        return "Action items will be generated from the AI summary.";
    }

    public String generateKeyPoints() {
        return "Key points will be generated from the AI summary.";
    }
}