package com.degerli.kisakes.service.impl;

import com.degerli.kisakes.exception.UrlNotFoundException;
import com.degerli.kisakes.model.dto.UrlCreateRequest;
import com.degerli.kisakes.model.entity.Url;
import com.degerli.kisakes.repository.UrlRepository;
import com.degerli.kisakes.service.UrlService;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

  private static final String CHARS
      = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int SHORT_CODE_LENGTH = 7;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final UrlRepository urlRepository;
  private final RedisTemplate<String, String> redisTemplate;

  @Override
  @Transactional
  public Url createShortUrl(UrlCreateRequest request) {
    Url url = new Url();
    url.setOriginalUrl(request.originalUrl());
    url.setShortCode(generateUniqueShortCode());

    return urlRepository.save(url);
  }

  public String getOriginalUrl(String shortCode) {
    String cacheKey = "url:" + shortCode;
    String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

    if (cachedUrl != null) {
      log.info("Cache HIT: {}", shortCode);
      return cachedUrl;
    }

    log.info("Cache MISS: {}", shortCode);

    Url url = urlRepository.findByShortCode(shortCode)
        .orElseThrow(() -> new UrlNotFoundException(shortCode));

    redisTemplate.opsForValue().set(cacheKey, url.getOriginalUrl(), 1, TimeUnit.HOURS);

    return url.getOriginalUrl();
  }

  private String generateUniqueShortCode() {
    String shortCode;
    do {
      shortCode = RANDOM.ints(SHORT_CODE_LENGTH, 0, CHARS.length())
          .mapToObj(CHARS::charAt)
          .map(Object::toString)
          .collect(Collectors.joining());
    }
    while (urlRepository.findByShortCode(shortCode).isPresent());
    return shortCode;
  }
}