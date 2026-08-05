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

    // ---------------- SUMMARY ----------------
    public String generateSummary(String transcript) {

        try {
            String endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key="
                            + apiKey;

            URL url = new URL(endpoint);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text",
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
                            "Transcript:\\n" + transcript);

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

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, "UTF-8")
            );

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
            return "AI Summary generation failed: " + e.getMessage();
        }
    }

    // ---------------- ACTION ITEMS ----------------
    public String generateActionItems(String transcript) {

        try {
            String endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key="
                            + apiKey;

            URL url = new URL(endpoint);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            JsonObject textPart = new JsonObject();
            textPart.addProperty(
                    "text",
                    "Extract only the action items from this meeting transcript. " +
                            "Return a clean numbered list with short task descriptions. " +
                            "Do not include explanations or extra text.\n\nTranscript:\n"
                            + transcript
            );

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

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, "UTF-8")
            );

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
            return "Unable to generate action items.";
        }
    }

    // ---------------- KEY POINTS ----------------
    public String generateKeyPoints(String transcript) {

        try {
            String endpoint =
                    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key="
                            + apiKey;

            URL url = new URL(endpoint);
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            JsonObject textPart = new JsonObject();
            textPart.addProperty("text",
                    "Extract the key discussion points from the following meeting transcript. " +
                            "Return 5-8 short bullet points starting with a hyphen. " +
                            "Do not add markdown symbols, numbering, or extra explanations.\\n\\n" +
                            transcript);

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

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, "UTF-8")
            );

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
            return "Unable to generate key points.";
        }
    }
}