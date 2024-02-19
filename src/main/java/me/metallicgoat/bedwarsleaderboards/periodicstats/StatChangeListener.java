package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.event.player.PlayerStatChangeEvent;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class StatChangeListener implements Listener {
  @EventHandler
  public void onStatChangeEvent(PlayerStatChangeEvent event) {
    final PlayerStatSet statSet = getStatsSetById(event.getKey());

    if (statSet == null || statSet instanceof PeriodicStatSet)
      return;

    final PlayerStats stats = event.getStats();
    final Number change = event.getNewValue().doubleValue() - event.getOldValue().doubleValue();

    // Update all periodic stats
    for (PeriodicStatSetType type : PeriodicStatSetType.values())
      stats.add(type.getId(statSet), change);
  }

  private PlayerStatSet getStatsSetById(String statKey) {
    for (PlayerStatSet statSet : PlayerDataAPI.get().getRegisteredStatSets()) {
      if (statSet.getId().equals(statKey)){
        return statSet;
      }
    }

    return null;
  }
}
