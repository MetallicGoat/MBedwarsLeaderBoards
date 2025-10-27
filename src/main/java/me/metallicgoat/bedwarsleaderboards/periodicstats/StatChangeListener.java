package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.event.player.PlayerStatChangeEvent;
import de.marcely.bedwars.api.player.DefaultPlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import me.metallicgoat.bedwarsleaderboards.Config;
import me.metallicgoat.bedwarsleaderboards.Util;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class StatChangeListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onStatChangeEvent(PlayerStatChangeEvent event) {
    if (!Config.customStatsTracking || event.isFromRemoteServer() /* otherwise we have an endless cycle of updates */)
      return;

    final PlayerStatSet changingStatSet = Util.getStatsSetById(event.getKey());

    // we don't want to track our own stats
    if (changingStatSet instanceof CustomTrackedStatSet)
      return;

    final PlayerStats stats = event.getStats();
    final Player player = Bukkit.getPlayer(stats.getPlayerUUID());
    final Arena arena = player != null ? GameAPI.get().getArenaByPlayer(player) : null;
    final Number change = event.getNewValue().doubleValue() - event.getOldValue().doubleValue();

    // Update all custom stats
    for (CustomTrackedStatSet customStatSet : Config.customStatSets) {

      updateRatios(stats, changingStatSet, customStatSet, change);

      if (customStatSet.getTrackedStatSet() == changingStatSet && customStatSet.isSupportedInArena(arena)) {
        stats.add(customStatSet.getId(), change);
      }
    }
  }


  // Extra tracking for the ratios
  private void updateRatios(PlayerStats stats, PlayerStatSet changingStatSet, CustomTrackedStatSet customStatSet, Number change) {
    if (customStatSet.getTrackedStatSet() == DefaultPlayerStatSet.K_D) {
      updateRatio(
          stats, customStatSet, changingStatSet,
          DefaultPlayerStatSet.KILLS,
          DefaultPlayerStatSet.DEATHS,
          change
      );

      return;
    }

    if (customStatSet.getTrackedStatSet() == DefaultPlayerStatSet.FINAL_K_D) {
      updateRatio(
          stats, customStatSet, changingStatSet,
          DefaultPlayerStatSet.FINAL_KILLS,
          DefaultPlayerStatSet.FINAL_DEATHS,
          change
      );

      return;
    }

    if (customStatSet.getTrackedStatSet() == DefaultPlayerStatSet.W_L) {
      updateRatio(
          stats, customStatSet, changingStatSet,
          DefaultPlayerStatSet.WINS,
          DefaultPlayerStatSet.LOSES,
          change
      );

      return;
    }
  }

  private void updateRatio(PlayerStats stats, PlayerStatSet customStatSet, PlayerStatSet changingStatSet, PlayerStatSet numerator, PlayerStatSet denominator, Number change) {
    // int part represents numerator, denominator
    if (changingStatSet == numerator) {
      stats.add(customStatSet.getId(), change.doubleValue());

    } else if (changingStatSet == denominator) {
      stats.add(customStatSet.getId(), change.doubleValue() * Math.pow(10, -CustomTrackedStatSet.RATIO_OFFSET_DIGITS));
    }
  }
}
