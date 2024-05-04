package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.message.Message;
import de.marcely.bedwars.api.player.*;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
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
  // %MBLeaderboards_playerstat-<statId>%
  @Override
  public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
    final String[] parts = params.split("-");
    final int size = parts.length;

    // Missing parameters
    if (size < 2)
      return "IMPROPER FORMAT";

    final String placeholderType = parts[0].toLowerCase();
    final PlayerStatSet statSet = Util.getStatsSetById(parts[1].toLowerCase());

    // Invalid stat ID
    if (statSet == null)
      return "INVALID STAT ID";

    switch (placeholderType) {
      case "playeratposition":
      case "valueatposition": {
        if (size < 3)
          return "IMPROPER FORMAT";

        final boolean returnValue = placeholderType.startsWith("value");
        final Integer position = parseInt(parts[2]);

        if (position == null)
          return "POSITION NULL";

        final LeaderboardFetchResult result = LeaderboardsPlugin.getCache().getCachedFetchResult(statSet, position);

        if (result == null)
          return getDataLoadingMessage(offlinePlayer.getPlayer());

        final PlayerProperties playerProperties = result.getPropertiesAtRank(position);

        // This is no player at this rank!
        if (playerProperties == null)
          return Message.build(Config.unfilledRank).done(offlinePlayer.getPlayer());

        if (!returnValue) { // get the name of the player at the rank
          return playerProperties.get(DefaultPlayerProperty.BASE_USERNAME).orElse("Unknown");

        } else { // Get the value at the players rank
          final Optional<PlayerStats> playerStats = PlayerDataAPI.get().getStatsCached(playerProperties.getPlayerUUID());

          if (playerStats.isPresent())
            return String.valueOf(statSet.getValue(playerStats.get()).intValue());

          // Cache da stats
          PlayerDataAPI.get().getStats(playerProperties.getPlayerUUID(), (stats) -> {});

          return getDataLoadingMessage(offlinePlayer.getPlayer());
        }
      }

      case "playerposition": {
        final Integer position = LeaderboardsPlugin.getCache().getCachedPlayerRank(offlinePlayer.getUniqueId(), statSet);

        if (position != null)
          return String.valueOf(position);

        // The player joined, but it's stilling being async cached
        return getDataLoadingMessage(offlinePlayer.getPlayer());
      }

      case "playerstat": {
        final Optional<PlayerStats> optional = PlayerDataAPI.get().getStatsCached(offlinePlayer);

        if (!optional.isPresent()) {
          // load it async
          PlayerDataAPI.get().getStats(offlinePlayer, (garbage) -> {});

          // return that it's still loading
          return getDataLoadingMessage(offlinePlayer.getPlayer());
        }

        return statSet.getDisplayedValue(optional.get());
      }

      default:
        return "IMPROPER FORMAT";
    }
  }
  
  private String getDataLoadingMessage(Player player) {
    return Message.build(Config.dataLoading).done(player);
  }

  private @Nullable Integer parseInt(String val) {
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException exception) {
      return null;
    }
  }
}
