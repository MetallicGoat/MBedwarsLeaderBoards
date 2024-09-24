package me.metallicgoat.bedwarsleaderboards.periodicstats;

import java.time.Duration;
import java.time.ZonedDateTime;
import me.metallicgoat.bedwarsleaderboards.Config;

import java.time.temporal.TemporalAdjusters;

public enum PeriodicType {
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY,
  NEVER;

  public String getResetKey() {
    return "last_" + this.name().toLowerCase() + "_time";
  }

  public boolean isOfType(String statId) {
    return statId.startsWith(this.name().toLowerCase() + ":");
  }

  public boolean needsReset(ZonedDateTime lastReset, ZonedDateTime now) {
    switch (this) {
      case DAILY:
        return lastReset.toLocalDate().isBefore(now.toLocalDate());

      case WEEKLY:
        final ZonedDateTime lastTuesday = lastReset.with(TemporalAdjusters.previousOrSame(Config.resetDay));
        final ZonedDateTime nextTuesday = lastTuesday.with(TemporalAdjusters.next(Config.resetDay));
        return now.isEqual(nextTuesday) || now.isAfter(nextTuesday);

      case MONTHLY:
        return lastReset.getMonthValue() != now.getMonthValue() || lastReset.getYear() != now.getYear();

      case YEARLY:
        return lastReset.getYear() < now.getYear();

      case NEVER:
        return false;

      default:
        throw new RuntimeException("Invalid stats set");
    }
  }
}
