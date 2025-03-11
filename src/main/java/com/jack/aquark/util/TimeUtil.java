package com.jack.aquark.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeUtil {
  public static boolean isPeak(LocalDateTime dateTime) {
    DayOfWeek day = dateTime.getDayOfWeek();
    LocalTime time = dateTime.toLocalTime();

    switch (day) {
      case THURSDAY:
      case FRIDAY:
        return true; // entire day peak
      case SATURDAY:
      case SUNDAY:
        return false; // entire day off-peak
      case MONDAY:
      case TUESDAY:
      case WEDNESDAY:
        // 7:30 ~ 17:30 => peak
        LocalTime startPeak = LocalTime.of(7, 30);
        LocalTime endPeak = LocalTime.of(17, 30);
        return !time.isBefore(startPeak) && !time.isAfter(endPeak);
      default:
        return false;
    }
  }
}
