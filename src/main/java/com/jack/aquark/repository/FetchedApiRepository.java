package com.jack.aquark.repository;

import com.jack.aquark.entity.FetchedApi;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FetchedApiRepository extends JpaRepository<FetchedApi, Long> {
    Optional<FetchedApi> findByApiUrl(String apiUrl);
}
