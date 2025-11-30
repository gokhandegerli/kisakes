package com.degerli.kisakes.controller;

import com.degerli.kisakes.model.dto.UrlCreateRequest;
import com.degerli.kisakes.model.dto.UrlResponse;
import com.degerli.kisakes.model.entity.Url;
import com.degerli.kisakes.service.UrlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
public class UrlController {

  private final UrlService urlService;

  @PostMapping("/api/v1/urls")
  public ResponseEntity<UrlResponse> createShortUrl(
      @Valid
      @RequestBody
      UrlCreateRequest request, HttpServletRequest servletRequest) {
    Url createdUrl = urlService.createShortUrl(request);
    UrlResponse response = toUrlResponse(createdUrl, servletRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{shortCode}")
  public ResponseEntity<Void> redirectToOriginalUrl(
      @PathVariable
      String shortCode) {
    String originalUrl = urlService.getOriginalUrlAndIncrementClicks(shortCode);
    return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(originalUrl)).build();
  }

  private UrlResponse toUrlResponse(Url url, HttpServletRequest request) {
    String shortUrl = ServletUriComponentsBuilder.fromContextPath(request)
        .path("/{shortCode}")
        .buildAndExpand(url.getShortCode())
        .toUriString();

    return new UrlResponse(url.getOriginalUrl(), shortUrl, url.getCreatedAt(),
        url.getExpiresAt());
  }
}