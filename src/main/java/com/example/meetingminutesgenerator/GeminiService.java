package com.example.meetingminutesgenerator;

import com.google.gson.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private String callGemini(String prompt) {
        try {
            String endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text", prompt);

            JsonArray parts = new JsonArray();
            parts.add(textPart);

            JsonObject content = new JsonObject();
            content.add("parts", parts);

            JsonArray contents = new JsonArray();
            contents.add(content);

            JsonObject body = new JsonObject();
            body.add("contents", contents);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.toString().getBytes("UTF-8"));
            }

            int status = connection.getResponseCode();

            InputStream stream = (status >= 200 && status < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            if (status >= 300) {
                return "Gemini API Error: " + response.toString();
            }

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();

            return json.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

        } catch (Exception e) {
            return "Gemini API Error: " + e.getMessage();
        }
    }

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

    public String generateActionItems(String transcript) {
        String prompt =
                "Extract only the action items from this meeting transcript. " +
                        "Return a clean numbered list with short task descriptions. " +
                        "Do not include explanations or extra text.\\n\\nTranscript:\\n" + transcript;

        return callGemini(prompt);
    }

    public String generateKeyPoints(String transcript) {
        String prompt =
                "Extract the key discussion points from the following meeting transcript. " +
                        "Return 5-8 short bullet points starting with a hyphen. " +
                        "Do not add markdown symbols, numbering, or extra explanations.\\n\\n" +
                        transcript;

        return callGemini(prompt);
    }
}