package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import me.metallicgoat.bedwarsleaderboards.Config;
import me.metallicgoat.bedwarsleaderboards.Console;
import me.metallicgoat.bedwarsleaderboards.LeaderboardsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

public class PeriodicStatResetter {
  private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  private static BukkitTask resetTask;

  public static void startResettingTask() {
    if (resetTask != null || Config.customStatsTracking)
      return;

    resetTask = Bukkit.getScheduler().runTaskTimerAsynchronously(LeaderboardsPlugin.getInstance(), () -> {
      PlayerDataAPI.get().getProperties(new UUID(0, 0), (properties) -> {

        for (PeriodicType type : PeriodicType.values()) {
          if (type == PeriodicType.NEVER)
            continue;

          final String resetKey = type.getResetKey();
          final Optional<String> optionalDate = properties.get(resetKey);

          if (!optionalDate.isPresent()) {
            properties.set(resetKey, getCurrentDate());
            continue;
          }

          final LocalDateTime dateTime = LocalDateTime.parse(optionalDate.get(), dateTimeFormatter);

          if (!type.needsReset(dateTime))
            continue;

          Console.printInfo("Resetting all " + type.name() + " stats!");
          properties.set(resetKey, getCurrentDate());

          // Reset all periodic stats of this type
          for (CustomTrackedStatSet statSet : Config.customStatSets) {
            if (statSet.getPeriodicType() == type) {
              purgePlayerStatsSet(statSet);
            }
          }
        }
      });
    }, 0, 20L * 60 * 5); // Check for any resets every 5 min
  }

  private static String getCurrentDate() {
    return LocalDateTime.now().format(dateTimeFormatter);
  }

  // Resets all sets of a certian type
  private static void purgePlayerStatsSet(CustomTrackedStatSet statSet) {
    for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
      PlayerDataAPI.get().getStats(player, playerStats -> {
        statSet.setValue(playerStats, 0);
      });
    }
  }
}
