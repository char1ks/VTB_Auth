package com.example.VTB_Hackathon.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class LLMControl {

    public String generate(String userPrompt) {
        return generate("", userPrompt, 0.000000001, 0);
    }

    public String generate(String systemPrompt, String userPrompt) {
        return generate(systemPrompt, userPrompt, 0.000000001, 0);
    }

    public String generate(String userPrompt, double temperature) {
        return generate("", userPrompt, temperature, 0);
    }

    public String generate(String userPrompt, int maxTokens) {
        return generate("", userPrompt, -1, maxTokens);
    }

    public String generate(String userPrompt, double temperature, int maxTokens) {
        return generate("", userPrompt, temperature, maxTokens);
    }

    public String generate(String systemPrompt, String userPrompt, double temperature) {
        return generate(systemPrompt, userPrompt, temperature, 0);
    }

    public String generate(String systemPrompt, String userPrompt, int maxTokens) {
        return generate(systemPrompt, userPrompt, -1, maxTokens);
    }

    public String generate(String systemPrompt, String userPrompt, double temperature, int maxTokens) {
        HttpURLConnection conn = null;
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL("http://localhost:8889/v1/chat/completions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Формируем JSON
            String jsonInputString = String.format("""
                {
                "messages": [
                  { "role": "system", "content": "%s" },
                  { "role": "user", "content": "%s" }
                ]
                """ + (temperature == 0.000000001 ? "" : ",\n \"temperature\": " + temperature) + """
                """ + (maxTokens == 0 ? "" : ",\n \"max_tokens\": " + maxTokens) + """
                }
                """,
                    systemPrompt.isEmpty() ? " " : systemPrompt,
                    userPrompt.isEmpty() ? " " : userPrompt);

            System.out.println("Отправляемый JSON: " + jsonInputString);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return getMessageFromResponse(response.toString());
    }

    private String getMessageFromResponse(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map;
        try {
            map = objectMapper.readValue(response, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Неверный формат Json: " + e.getMessage());
        }

        // Проверка наличия ключа "choices"
        if (!map.containsKey("choices") || map.get("choices") == null) {
            throw new RuntimeException("Поле 'choices' отсутствует или равно null в ответе");
        }

        // Приведение к типу
        ArrayList<LinkedHashMap> choices = (ArrayList<LinkedHashMap>) map.get("choices");

        // Проверка, что список не пуст
        if (choices.isEmpty()) {
            throw new RuntimeException("Список 'choices' пуст");
        }

        // Извлечение сообщения
        String message = choices.get(0).get("message").toString();
        return message.substring(message.indexOf("content=") + 8, message.length() - 1);
    }
}