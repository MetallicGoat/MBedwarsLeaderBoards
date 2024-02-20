package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.event.player.PlayerStatChangeEvent;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import me.metallicgoat.bedwarsleaderboards.Config;
import me.metallicgoat.bedwarsleaderboards.Util;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class StatChangeListener implements Listener {
  @EventHandler
  public void onStatChangeEvent(PlayerStatChangeEvent event) {
    if (!Config.periodicStatsEnabled)
      return;

    final PlayerStatSet statSet = Util.getStatsSetById(event.getKey());

    if (statSet == null || Util.isPeriodicStat(statSet) || !Config.statsTrackedPeriodically.contains(statSet.getId()))
      return;

    final PlayerStats stats = event.getStats();
    final Number change = event.getNewValue().doubleValue() - event.getOldValue().doubleValue();

    // Update all periodic stats
    for (PeriodicStatSetType type : Config.periodicStatsTracked)
      stats.add(type.getId(statSet), change);
  }
}
