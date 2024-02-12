package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerProperties;
import de.marcely.bedwars.api.player.PlayerStatSet;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Placeholders extends PlaceholderExpansion {

  private final LeaderboardsPlugin plugin;

  public Placeholders(LeaderboardsPlugin plugin){
    this.plugin = plugin;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "MBLeaderboards";
  }

  @Override
  public @NotNull String getAuthor() {
    return "MetallicGoat";
  }

  @Override
  public @NotNull String getVersion() {
    return this.plugin.getDescription().getVersion();
  }

  // %MBLeaderboards_playeratposition-<statId>-<position>%
  // %MBLeaderboards_playerposition-<statId>%
  @Override
  public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
    final String[] parts = params.split("-");
    final int size = parts.length;

    // Missing parameters
    if (size < 2)
      return null;

    final String placeholderType = parts[0].toLowerCase();
    final PlayerStatSet statSet = getPlayerStatsSet(parts[1].toLowerCase());

    // Invalid stat ID
    if (statSet == null)
      return null;

    switch (placeholderType) {
      case "playeratposition": {
        if (size < 3)
          return null;

        final Integer position = parseInt(parts[2]);

        if (position == null)
          return null;

        final PlayerProperties playerProperties = LeaderboardsCache.getPlayerAtPos(statSet, position);

        if (playerProperties == null)
          return "UNKNOWN";

        return Bukkit.getOfflinePlayer(playerProperties.getPlayerUUID()).getName();
      }

      case "playerposition": {
        return String.valueOf(LeaderboardsCache.getPlayerRank(offlinePlayer, statSet));
      }

      default:
        return null;
    }
  }

  private @Nullable Integer parseInt(String val) {
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException exception) {
      return null;
    }
  }

  private @Nullable PlayerStatSet getPlayerStatsSet(String name){
    for (PlayerStatSet statsSet : PlayerDataAPI.get().getRegisteredStatSets()){
      if (statsSet.getId().equals(name)){
        return statsSet;
      }
    }

    return null;
  }
}
