package com.wuyou.llmutils.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ChatRequest {
    private String model;
    private List<ChatMessage> messages;
    @Builder.Default
    private Double temperature = 0.7;
    private Boolean stream;
}
