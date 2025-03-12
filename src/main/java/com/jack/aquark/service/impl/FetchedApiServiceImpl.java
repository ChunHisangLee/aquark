package com.jack.aquark.service.impl;

import com.jack.aquark.entity.FetchedApi;
import com.jack.aquark.repository.FetchedApiRepository;
import com.jack.aquark.service.FetchedApiService;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FetchedApiServiceImpl implements FetchedApiService {

  private final FetchedApiRepository fetchedApiRepository;

  @Override
  public boolean exists(String apiUrl) {
    return fetchedApiRepository.findByApiUrl(apiUrl).isPresent();
  }

  @Override
  public void saveApiUrl(String apiUrl) {
    FetchedApi fetchedApi =
        FetchedApi.builder().apiUrl(apiUrl).fetchedAt(LocalDateTime.now()).build();
    fetchedApiRepository.save(fetchedApi);
  }
}
