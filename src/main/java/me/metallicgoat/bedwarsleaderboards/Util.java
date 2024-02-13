package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;

import java.util.Collection;
import java.util.stream.Collectors;

public class Util {

  public static Collection<PlayerStatSet> getSupportedStats() {
    return PlayerDataAPI.get().getRegisteredStatSets().stream()
        .filter(statSet -> !statSet.getId().equals("bedwars:rank"))
        .collect(Collectors.toList());

  }

}
