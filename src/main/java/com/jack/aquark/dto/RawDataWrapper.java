package com.jack.aquark.dto;

import java.util.List;
import lombok.Data;

@Data
public class RawDataWrapper {
  private List<RawDataItem> raw;
}
