package com.example.spring_boot_project_api.dto.response;

import java.time.Instant;
import java.util.List;

public record ConnectedAccountsResponse(List<String> providers, Instant canLinkUntil) {
}
