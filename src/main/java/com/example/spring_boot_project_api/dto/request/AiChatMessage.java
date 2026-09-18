package com.example.spring_boot_project_api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatMessage {
    /**
     * Role of the speaker: "user" or "model" (or "assistant")
     */
    private String role;

    /**
     * Content of the message turn
     */
    private String content;
}
