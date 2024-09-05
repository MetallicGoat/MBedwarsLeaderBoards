package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.event.player.PlayerStatChangeEvent;
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

  @EventHandler(priority = EventPriority.MONITOR)
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
      if (customStatSet.getTrackedStatSet() == statSet && customStatSet.isSupportedInArena(arena)) {
        stats.add(customStatSet.getId(), change);
      }
    }
  }
}
