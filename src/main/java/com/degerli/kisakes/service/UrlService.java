package com.degerli.kisakes.service;


import com.degerli.kisakes.model.dto.UrlCreateRequest;
import com.degerli.kisakes.model.entity.Url;

public interface UrlService {

  /**
   * Verilen orijinal URL için benzersiz bir kısa URL oluşturur ve kaydeder.
   *
   * @param request Orijinal URL'i içeren istek nesnesi.
   * @return Kaydedilen Url entity'si.
   */
  Url createShortUrl(UrlCreateRequest request);

  /**
   * Verilen kısa koda karşılık gelen orijinal URL'i bulur ve tıklanma sayısını artırır.
   *
   * @param shortCode Yönlendirme için kullanılacak kısa kod.
   * @return Orijinal URL string'i.
   */
  String getOriginalUrlAndIncrementClicks(String shortCode);
}