package com.example.meetingminutesgenerator;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String callGemini(String prompt) {
        try {
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );

            return response.text();

        } catch (Exception e) {
            return "Gemini API Error: " + e.getMessage();
        }
    }

    // ---------------- SUMMARY ----------------
    public String generateSummary(String transcript) {
        String prompt =
                "You are a professional meeting minutes assistant. " +
                        "Generate clean and well-formatted meeting minutes from the following transcript. " +
                        "Do NOT use markdown symbols such as **, ###, or quotes. " +
                        "Return the output in this exact format:\\n\\n" +
                        "Meeting Summary\\n" +
                        "Write a concise summary in 4-6 sentences.\\n\\n" +
                        "Main Decisions\\n" +
                        "- Decision 1\\n" +
                        "- Decision 2\\n" +
                        "- Decision 3\\n\\n" +
                        "Next Steps\\n" +
                        "- Task 1\\n" +
                        "- Task 2\\n" +
                        "- Task 3\\n\\n" +
                        "Transcript:\\n" + transcript;

        return callGemini(prompt);
    }

    // ---------------- ACTION ITEMS ----------------
    public String generateActionItems(String transcript) {
        String prompt =
                "Extract only the action items from this meeting transcript. " +
                        "Return a clean numbered list with short task descriptions. " +
                        "Do not include explanations or extra text.\\n\\nTranscript:\\n" + transcript;

        return callGemini(prompt);
    }

    // ---------------- KEY POINTS ----------------
    public String generateKeyPoints(String transcript) {
        String prompt =
                "Extract the key discussion points from the following meeting transcript. " +
                        "Return 5-8 short bullet points starting with a hyphen. " +
                        "Do not add markdown symbols, numbering, or extra explanations.\\n\\n" +
                        transcript;

        return callGemini(prompt);
    }
}