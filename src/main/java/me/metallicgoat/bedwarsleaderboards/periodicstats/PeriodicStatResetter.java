package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Set;
import me.metallicgoat.bedwarsleaderboards.Config;
import me.metallicgoat.bedwarsleaderboards.Console;
import me.metallicgoat.bedwarsleaderboards.LeaderboardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PeriodicStatResetter {
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static BukkitTask resetTask;

  public static void startResettingTask() {
    if (resetTask != null || !Config.customStatsTracking)
      return;

    resetTask = Bukkit.getScheduler().runTaskTimer(LeaderboardsPlugin.getInstance(), () -> {
      PlayerDataAPI.get().getProperties(new UUID(0, 0), (properties) -> {
        for (PeriodicType type : PeriodicType.values()) {
          if (type == PeriodicType.NEVER)
            continue;

          final ZoneId zone = Config.timeZone;
          final ZonedDateTime now = ZonedDateTime.now(zone);
          final String resetKey = type.getResetKey();
          final Optional<String> optionalDate = properties.get(resetKey);

          if (!optionalDate.isPresent()) {
            properties.set(resetKey, now.format(dateTimeFormatter));
            continue;
          }

          final ZonedDateTime dateTime = LocalDateTime
              .parse(optionalDate.get(), dateTimeFormatter)
              .atZone(zone);

          if (!type.needsReset(dateTime, now))
            continue;

          properties.set(resetKey, now.format(dateTimeFormatter));

          final List<CustomTrackedStatSet> statsNeedingReset = Config.customStatSets.stream()
              .filter(statSet -> statSet.getPeriodicType() == type)
              .collect(Collectors.toList());

          if (statsNeedingReset.isEmpty())
            continue;

          Console.printInfo("Resetting " + statsNeedingReset.size() + " " + type.name() + " stat set(s)!");

          purgePlayerStatsSet(statsNeedingReset);
        }
      });
    }, 0, 20L * 60 * 5); // Check for any resets every 5 min
  }

  // Resets all sets
  private static void purgePlayerStatsSet(Collection<CustomTrackedStatSet> statSets) {
    final Set<String> statSetIds = statSets.stream()
        .map(CustomTrackedStatSet::getId)
        .collect(Collectors.toSet());

    PlayerDataAPI.get().purgeAllPlayerData(
        statSetIds,
        false,
        false,
        false,
        null);
  }
}
