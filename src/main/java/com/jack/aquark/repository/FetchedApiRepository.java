package com.jack.aquark.repository;

import com.jack.aquark.entity.FetchedApi;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FetchedApiRepository extends JpaRepository<FetchedApi, Long> {
  Optional<FetchedApi> findByApiUrl(String apiUrl);
}
