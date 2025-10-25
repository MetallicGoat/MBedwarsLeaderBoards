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

    final PlayerStatSet statSet = Util.getStatsSetById(event.getKey());

    // we don't want to track our own stats
    if (statSet instanceof CustomTrackedStatSet)
      return;

    final PlayerStats stats = event.getStats();
    final Player player = Bukkit.getPlayer(stats.getPlayerUUID());
    final Arena arena = player != null ? GameAPI.get().getArenaByPlayer(player) : null;
    final Number change = event.getNewValue().doubleValue() - event.getOldValue().doubleValue();

    // Update all custom stats
    for (CustomTrackedStatSet customStatSet : Config.customStatSets) {

      updateRatios(stats, statSet, customStatSet, change);

      if (customStatSet.getTrackedStatSet() == statSet && customStatSet.isSupportedInArena(arena)) {
        stats.add(customStatSet.getId(), change);
      }
    }
  }


  // Extra tracking for the ratios
  private void updateRatios(PlayerStats stats, PlayerStatSet statSet, CustomTrackedStatSet customStatSet, Number change) {
    if (customStatSet.getTrackedStatSet() == DefaultPlayerStatSet.K_D) {

      // int part represents kills, decimal part represents deaths
      if (statSet == DefaultPlayerStatSet.KILLS) {
        stats.add(customStatSet.getId(), change.doubleValue());

      } else if (statSet == DefaultPlayerStatSet.DEATHS) {
        stats.add(customStatSet.getId(), Math.pow(10, change.doubleValue() * -CustomTrackedStatSet.RATIO_OFFSET_DIGITS));
      }
    }

    if (customStatSet.getTrackedStatSet() == DefaultPlayerStatSet.FINAL_K_D) {

      // int part represents final kills, decimal part represents final deaths
      if (statSet == DefaultPlayerStatSet.FINAL_KILLS) {
        stats.add(customStatSet.getId(), change.doubleValue());

      } else if (statSet == DefaultPlayerStatSet.FINAL_DEATHS) {
        stats.add(customStatSet.getId(), Math.pow(10, change.doubleValue() * -CustomTrackedStatSet.RATIO_OFFSET_DIGITS));
      }
    }

    if (customStatSet.getTrackedStatSet() == DefaultPlayerStatSet.W_L) {

      // int part represents wins, decimal part represents loses
      if (statSet == DefaultPlayerStatSet.WINS) {
        stats.add(customStatSet.getId(), change.doubleValue());

      } else if (statSet == DefaultPlayerStatSet.LOSES) {
        stats.add(customStatSet.getId(), Math.pow(10, change.doubleValue() * -CustomTrackedStatSet.RATIO_OFFSET_DIGITS));
      }
    }
  }
}
