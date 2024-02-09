package me.metallocgoat.bedwarsleaderboards;

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

  @Override
  public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
    final String[] parts = params.split("-");

    final PlayerStatSet statSet = getPlayerStatsSet(parts[0]);
    final Integer position = parseInt(parts[1]);

    if (statSet == null || position == null)
      return "INVALID PLACEHOLDER"; // Incorrect placeholder

    final PlayerProperties playerProperties = LeaderboardsCache.getPlayerAtPos(statSet, position);

    return Bukkit.getOfflinePlayer(playerProperties.getPlayerUUID()).getName();
  }

  private Integer parseInt(String val) {
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
