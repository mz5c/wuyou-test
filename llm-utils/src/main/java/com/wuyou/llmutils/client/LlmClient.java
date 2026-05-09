package com.wuyou.llmutils.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wuyou.llmutils.model.ChatMessage;
import com.wuyou.llmutils.model.ChatRequest;
import com.wuyou.llmutils.model.ChatResponse;
import com.wuyou.llmutils.properties.LlmProperties;
import com.wuyou.llmutils.streaming.StreamCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Slf4j
public class LlmClient {

    private final RestTemplate restTemplate;
    private final LlmProperties properties;
    private final ObjectMapper objectMapper;

    public LlmClient(LlmProperties properties, RestTemplateBuilder builder) {
        this.properties = properties;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(properties.getTimeout()))
                .setReadTimeout(Duration.ofMillis(properties.getTimeout()))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /** 同步调用 */
    public ChatResponse chat(ChatRequest request) {
        request.setStream(false);
        if (null == request.getModel() || request.getModel().isBlank()) {
            request.setModel(properties.getModel());
        }
        HttpEntity<ChatRequest> entity = buildEntity(request);
        long start = System.currentTimeMillis();
        log.info("LLM request URL={}, model={}", properties.getFullUrl(), request.getModel());
        try {
            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    properties.getFullUrl(),
                    HttpMethod.POST,
                    entity,
                    ChatResponse.class);
            log.info("LLM response success, cost={}ms", System.currentTimeMillis() - start);
            return response.getBody();
        } catch (Exception e) {
            log.error("LLM request failed, cost={}ms, URL={}, model={}",
                    System.currentTimeMillis() - start, properties.getFullUrl(), request.getModel(), e);
            throw new RuntimeException("LLM call failed: " + e.getMessage(), e);
        }
    }

    /** 流式调用 */
    public void chatStream(ChatRequest request, StreamCallback callback) {
        request.setStream(true);
        if (null == request.getModel() || request.getModel().isBlank()) {
            request.setModel(properties.getModel());
        }
        HttpEntity<ChatRequest> entity = buildEntity(request);
        long start = System.currentTimeMillis();
        log.info("LLM stream request URL={}, model={}", properties.getFullUrl(), request.getModel());
        try {
            ResponseEntity<Resource> response = restTemplate.exchange(
                    properties.getFullUrl(),
                    HttpMethod.POST,
                    entity,
                    Resource.class);
            ChatResponse fullResponse = new ChatResponse();
            ChatResponse.Choice choice = new ChatResponse.Choice();
            ChatMessage message = new ChatMessage("assistant", "");
            choice.setMessage(message);
            fullResponse.setChoices(List.of(choice));

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            Objects.requireNonNull(response.getBody()).getInputStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                StringBuilder contentBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JsonNode root = objectMapper.readTree(data);
                            JsonNode choices = root.get("choices");
                            if (choices != null && choices.isArray() && choices.size() > 0) {
                                JsonNode delta = choices.get(0).get("delta");
                                if (delta != null && delta.has("content")) {
                                    String token = delta.get("content").asText();
                                    callback.onToken(token);
                                    contentBuilder.append(token);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse SSE data line: {}", data, e);
                        }
                    }
                }
                fullResponse.getChoices().get(0).getMessage().setContent(contentBuilder.toString());
                log.info("LLM stream completed, cost={}ms, tokens={}",
                        System.currentTimeMillis() - start, contentBuilder.length());
                callback.onComplete(fullResponse);
            }
        } catch (Exception e) {
            log.error("LLM stream failed, cost={}ms, URL={}, model={}",
                    System.currentTimeMillis() - start, properties.getFullUrl(), request.getModel(), e);
            callback.onError(e);
        }
    }

    private HttpEntity<ChatRequest> buildEntity(ChatRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());
        return new HttpEntity<>(request, headers);
    }
}
