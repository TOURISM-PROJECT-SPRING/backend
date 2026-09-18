package com.example.spring_boot_project_api.dto.request;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @NotBlank(message = "User message cannot be blank")
    private String message;

    @Builder.Default
    private List<AiChatMessage> conversationHistory = new ArrayList<>();

    private String destination;

    private String language;
}
