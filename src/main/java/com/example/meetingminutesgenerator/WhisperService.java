package com.example.meetingminutesgenerator;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class WhisperService {

    @Value("${openai.api.key}")
    private String apiKey;

    public String transcribe(File audioFile) throws IOException {

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.parse("audio/mpeg"))
                )
                .addFormDataPart("model", "whisper-1")
                .build();

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Whisper API Error: " + response.body().string());
            }

            String json = response.body().string();

            JsonObject object = JsonParser.parseString(json).getAsJsonObject();

            return object.get("text").getAsString();
        }
    }
}