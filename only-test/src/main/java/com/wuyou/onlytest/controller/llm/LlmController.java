package com.wuyou.onlytest.controller.llm;

import cn.hutool.json.JSONObject;
import com.wuyou.common.result.Result;
import com.wuyou.llmutils.client.LlmClient;
import com.wuyou.llmutils.model.ChatMessage;
import com.wuyou.llmutils.model.ChatRequest;
import com.wuyou.llmutils.model.ChatResponse;
import com.wuyou.llmutils.properties.LlmProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Tag(name = "LLM 聊天", description = "大语言模型聊天接口测试")
@RestController
@RequestMapping("/api/v1/llm")
@RequiredArgsConstructor
public class LlmController {

    private final LlmClient llmClient;

    @Operation(summary = "非流式聊天", description = "发送 prompt，等待完整响应后返回")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody LlmChatRequest request) {
        ChatRequest chatRequest = buildRequest(request, false);
        ChatResponse response = llmClient.chat(chatRequest);
        String content = extractContent(response);
        return Result.success(content);
    }

    @Operation(summary = "流式聊天 (SSE)", description = "发送 prompt，通过 SSE 逐 token 返回响应")
    @PostMapping(value = "/chat/stream", produces = "text/event-stream")
    public SseEmitter chatStream(@RequestBody LlmChatRequest request) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 分钟超时

        ChatRequest chatRequest = buildRequest(request, true);

        CompletableFuture.runAsync(() -> {
            try {
                llmClient.chatStream(chatRequest, new com.wuyou.llmutils.streaming.StreamCallback() {
                    @Override
                    public void onToken(String token) {
                        try {
                            JSONObject jo = new JSONObject();
                            jo.set("content", token);
                            emitter.send(SseEmitter.event()
                                    .name("token")
                                    .data(jo.toString()));
                        } catch (IOException e) {
                            throw new RuntimeException("SSE send failed", e);
                        }
                    }

                    @Override
                    public void onComplete(ChatResponse response) {
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("[DONE]"));
                            emitter.complete();
                        } catch (IOException e) {
                            log.error("Failed to send done event", e);
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("Stream error", error);
                        emitter.completeWithError(error);
                    }
                });
            } catch (Exception e) {
                log.error("Stream failed", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private ChatRequest buildRequest(LlmChatRequest request, boolean stream) {
        ChatMessage userMessage = ChatMessage.builder()
                .role("user")
                .content(request.getPrompt())
                .build();

        ChatRequest.ChatRequestBuilder builder = ChatRequest.builder()
                .messages(List.of(userMessage))
                .stream(stream);

        if (request.getModel() != null && !request.getModel().isBlank()) {
            builder.model(request.getModel());
        }

        return builder.build();
    }

    private String extractContent(ChatResponse response) {
        if (response != null
                && response.getChoices() != null
                && !response.getChoices().isEmpty()
                && response.getChoices().get(0).getMessage() != null) {
            return response.getChoices().get(0).getMessage().getContent();
        }
        return "";
    }

    @Data
    public static class LlmChatRequest {
        private String prompt;
        private String model;
    }
}
