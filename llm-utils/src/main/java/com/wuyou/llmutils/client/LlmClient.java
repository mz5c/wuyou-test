package com.wuyou.llmutils.client;

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

    public LlmClient(LlmProperties properties, RestTemplateBuilder builder) {
        this.properties = properties;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofMillis(properties.getTimeout()))
                .setReadTimeout(Duration.ofMillis(properties.getTimeout()))
                .build();
    }

    /** 同步调用 */
    public ChatResponse chat(ChatRequest request) {
        request.setStream(false);
        HttpEntity<ChatRequest> entity = buildEntity(request);
        try {
            ResponseEntity<ChatResponse> response = restTemplate.exchange(
                    properties.getBaseUrl() + "/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    ChatResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("LLM sync call failed", e);
            throw new RuntimeException("LLM call failed: " + e.getMessage(), e);
        }
    }

    /** 流式调用 */
    public void chatStream(ChatRequest request, StreamCallback callback) {
        request.setStream(true);
        HttpEntity<ChatRequest> entity = buildEntity(request);
        try {
            ResponseEntity<Resource> response = restTemplate.exchange(
                    properties.getBaseUrl() + "/v1/chat/completions",
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
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        if (data.contains("\"delta\"") && data.contains("\"content\"")) {
                            int idx = data.indexOf("\"content\":\"") + 11;
                            if (idx > 10) {
                                int end = data.indexOf("\"", idx);
                                if (end > idx) {
                                    String token = data.substring(idx, end);
                                    callback.onToken(token);
                                    contentBuilder.append(token);
                                }
                            }
                        }
                    }
                }
                fullResponse.getChoices().get(0).getMessage().setContent(contentBuilder.toString());
                callback.onComplete(fullResponse);
            }
        } catch (Exception e) {
            log.error("LLM stream call failed", e);
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
