package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import me.metallicgoat.bedwarsleaderboards.periodicstats.PeriodicStatSet;
import me.metallicgoat.bedwarsleaderboards.periodicstats.PeriodicStatSetType;

public class Util {

  public static PlayerStatSet getStatsSetById(String statKey) {
    for (PlayerStatSet statSet : PlayerDataAPI.get().getRegisteredStatSets()) {
      if (statSet.getId().equals(statKey)) {
        return statSet;
      }
    }

    return null;
  }

  public static boolean isPeriodicStat(PlayerStatSet statSet) {
    if (statSet instanceof PeriodicStatSet)
      return true;

    for (PeriodicStatSetType type : PeriodicStatSetType.values())
      if (type.isOfType(statSet.getId()))
        return true;

    return false;
  }
}
