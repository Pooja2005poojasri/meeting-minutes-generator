package com.example.meetingminutesgenerator;

import com.google.gson.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient();

    private String callOpenAI(String prompt) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("model", "gpt-4.1-mini");

            JsonArray messages = new JsonArray();

            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", "You are a professional meeting minutes assistant.");
            messages.add(system);

            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", prompt);
            messages.add(user);

            body.add("messages", messages);

            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                return "OpenAI API Error: " + response.code() + " - " + response.body().string();
            }

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();

            return json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

        } catch (IOException e) {
            return "OpenAI API Error: " + e.getMessage();
        }
    }

    public String generateSummary(String transcript) {
        return callOpenAI(
                "Generate professional meeting minutes from this transcript. " +
                        "Include Meeting Summary, Main Decisions, and Next Steps.\\n\\nTranscript:\\n" + transcript
        );
    }

    public String generateActionItems(String transcript) {
        return callOpenAI(
                "Extract only the action items from this meeting transcript as a numbered list.\\n\\nTranscript:\\n" + transcript
        );
    }

    public String generateKeyPoints(String transcript) {
        return callOpenAI(
                "Extract the key discussion points from this meeting transcript as bullet points.\\n\\nTranscript:\\n" + transcript
        );
    }
}