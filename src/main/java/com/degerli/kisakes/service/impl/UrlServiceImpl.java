package com.degerli.kisakes.service.impl;

import com.degerli.kisakes.exception.UrlNotFoundException;
import com.degerli.kisakes.model.dto.UrlCreateRequest;
import com.degerli.kisakes.model.entity.Url;
import com.degerli.kisakes.repository.UrlRepository;
import com.degerli.kisakes.service.UrlService;
import java.security.SecureRandom;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

  private static final String CHARS
      = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final int SHORT_CODE_LENGTH = 7;
  private static final SecureRandom RANDOM = new SecureRandom();

  private final UrlRepository urlRepository;

  @Override
  @Transactional
  public Url createShortUrl(UrlCreateRequest request) {
    Url url = new Url();
    url.setOriginalUrl(request.originalUrl());
    url.setShortCode(generateUniqueShortCode());

    return urlRepository.save(url);
  }

  @Override
  @Transactional
  public String getOriginalUrlAndIncrementClicks(String shortCode) {

    Url url = urlRepository.findByShortCode(shortCode)
        .orElseThrow(() -> new UrlNotFoundException(shortCode));
    url.setClickCount(url.getClickCount() + 1);
    urlRepository.save(url);

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