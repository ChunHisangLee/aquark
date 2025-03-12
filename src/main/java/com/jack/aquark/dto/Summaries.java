package com.jack.aquark.dto;

import lombok.Data;

@Data
public class Summaries {
  private static final long serialVersionUID = 1L;
  // Sum fields for voltage readings
  private double sumV1;
  private double sumV2;
  private double sumV3;
  private double sumV4;
  private double sumV5;
  private double sumV6;
  private double sumV7;

  // Sum fields for other sensor values
  private double sumRh;
  private double sumTx;
  private double sumEcho;
  private double sumRainD;
  private double sumSpeed;

  // Average fields for voltage readings
  private double avgV1;
  private double avgV2;
  private double avgV3;
  private double avgV4;
  private double avgV5;
  private double avgV6;
  private double avgV7;

  // Average fields for other sensor values
  private double avgRh;
  private double avgTx;
  private double avgEcho;
  private double avgSpeed;
}
