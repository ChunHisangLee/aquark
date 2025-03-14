package com.jack.aquark.dto;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class SummariesDto implements Serializable {
  @Serial private static final long serialVersionUID = 1L;

  // Sum fields for voltage readings
  private BigDecimal sumV1;
  private BigDecimal sumV2;
  private BigDecimal sumV3;
  private BigDecimal sumV4;
  private BigDecimal sumV5;
  private BigDecimal sumV6;
  private BigDecimal sumV7;

  // Sum fields for other sensor values
  private BigDecimal sumRh;
  private BigDecimal sumTx;
  private BigDecimal sumEcho;
  private BigDecimal sumRainD;
  private BigDecimal sumSpeed;

  // Average fields for voltage readings
  private BigDecimal avgV1;
  private BigDecimal avgV2;
  private BigDecimal avgV3;
  private BigDecimal avgV4;
  private BigDecimal avgV5;
  private BigDecimal avgV6;
  private BigDecimal avgV7;

  // Average fields for other sensor values
  private BigDecimal avgRh;
  private BigDecimal avgTx;
  private BigDecimal avgEcho;
  private BigDecimal avgSpeed;
}
