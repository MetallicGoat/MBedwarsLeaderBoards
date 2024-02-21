package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import me.metallicgoat.bedwarsleaderboards.periodicstats.CustomTrackedStatSet;
import me.metallicgoat.bedwarsleaderboards.periodicstats.PeriodicType;

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
    if (statSet instanceof CustomTrackedStatSet)
      return true;

    for (PeriodicType type : PeriodicType.values())
      if (type.isOfType(statSet.getId()))
        return true;

    return false;
  }
}
