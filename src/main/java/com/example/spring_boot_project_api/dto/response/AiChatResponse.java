package com.example.spring_boot_project_api.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {

    private String reply;

    @Builder.Default
    private List<String> suggestedFollowUps = new ArrayList<>();

    @Builder.Default
    private List<ReferencedItemDto> referencedItems = new ArrayList<>();

    private String modelUsed;

    private boolean fallbackUsed;
}
