package com.jack.aquark.service;

public interface FetchedApiService {
  boolean exists(String apiUrl);

  void saveApiUrl(String apiUrl);
}
