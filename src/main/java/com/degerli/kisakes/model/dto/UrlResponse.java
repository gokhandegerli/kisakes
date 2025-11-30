package com.degerli.kisakes.model.dto;

import java.time.Instant;

public record UrlResponse(String originalUrl, String shortUrl, Instant createdAt,
    Instant expiresAt) {}