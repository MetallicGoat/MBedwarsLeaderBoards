package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.player.PlayerStatSet;
import me.metallicgoat.bedwarsleaderboards.Config;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public enum PeriodicStatSetType {
  DAILY,
  WEEKLY,
  MONTHLY,
  YEARLY;

  public String getId(PlayerStatSet statSet) {
    return this.getId(statSet.getId());
  }

  public String getId(String statSetKey) {
    return this.name().toLowerCase() + ":" + statSetKey;
  }

  public String getResetKey() {
    return "last_" + this.name().toLowerCase() + "_time";
  }

  public boolean isOfType(String statId) {
    return statId.startsWith(this.name().toLowerCase() + ":");
  }

  public boolean needsReset(LocalDateTime lastReset) {
    final LocalDateTime now = LocalDateTime.now();

    switch (this) {
      case DAILY:
        return lastReset.toLocalDate().isBefore(now.toLocalDate());

      case WEEKLY:
        final LocalDateTime lastTuesday = lastReset.with(TemporalAdjusters.previousOrSame(Config.resetDay));
        final LocalDateTime nextTuesday = lastTuesday.with(TemporalAdjusters.next(Config.resetDay));
        return now.isEqual(nextTuesday) || now.isAfter(nextTuesday);

      case MONTHLY:
        return lastReset.getMonthValue() != now.getMonthValue() || lastReset.getYear() != now.getYear();

      case YEARLY:
        return lastReset.getYear() < now.getYear();

      default:
        throw new RuntimeException("Invalid stats set");
    }
  }
}
