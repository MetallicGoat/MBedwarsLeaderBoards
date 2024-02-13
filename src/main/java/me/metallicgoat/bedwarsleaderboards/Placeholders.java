package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.message.Message;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerProperties;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
  // %MBLeaderboards_valueatposition-<statId>-<position>%
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
      case "playeratposition":
      case "valueatposition": {
        if (size < 3)
          return null;

        final boolean returnValue = placeholderType.startsWith("value");
        final Integer position = parseInt(parts[2]);

        if (position == null)
          return null;

        // Trying to parse a placeholder for a position that is not cached
        if (position > Config.positionsCached)
          return Message.build(Config.uncachedPosition).done(offlinePlayer.getPlayer());

        final PlayerProperties playerProperties = LeaderboardsCache.getPlayerAtPos(statSet, position);

        // This is no player at this rank!
        if (playerProperties == null)
          return Message.build(Config.unfilledRank).done(offlinePlayer.getPlayer());

        if (!returnValue) { // get the name of the player at the rank
          return Bukkit.getOfflinePlayer(playerProperties.getPlayerUUID()).getName();

        } else { // Get the value at the players rank
          final Optional<PlayerStats> playerStats = PlayerDataAPI.get().getStatsCached(playerProperties.getPlayerUUID());

          if (playerStats.isPresent())
            return String.valueOf((statSet.getValue(playerStats.get()).intValue()));

          // Cache da stats
          PlayerDataAPI.get().getStats(playerProperties.getPlayerUUID(), (stats) -> {});

          return Message.build(Config.dataLoading).done(offlinePlayer.getPlayer());
        }
      }

      case "playerposition": {
        final Integer position = LeaderboardsCache.getPlayerRank(offlinePlayer, statSet);

        if (position != null)
          return String.valueOf(position);

        // Its possible the player joined, it its stilling being async cached
        return Message.build(Config.dataLoading).done(offlinePlayer.getPlayer());
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
    for (PlayerStatSet statsSet : Util.getSupportedStats()){
      if (statsSet.getId().equals(name)){
        return statsSet;
      }
    }

    return null;
  }
}
