package com.jack.aquark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "fetched_api")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FetchedApi {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "api_url", unique = true, nullable = false)
  private String apiUrl;

  @Column(name = "fetched_at", nullable = false)
  private LocalDateTime fetchedAt;
}
