package com.wuyou.llmutils.streaming;

import com.wuyou.llmutils.model.ChatResponse;

@FunctionalInterface
public interface StreamCallback {
    void onToken(String token);

    default void onComplete(ChatResponse response) {}
    default void onError(Throwable error) {}
}
