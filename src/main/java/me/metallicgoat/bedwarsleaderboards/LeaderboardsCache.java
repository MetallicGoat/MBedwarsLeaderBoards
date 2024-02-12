package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.player.LeaderboardFetchResult;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerProperties;
import de.marcely.bedwars.api.player.PlayerStatSet;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardsCache implements Listener {
  private static BukkitTask cacheTask;

  public static Map<PlayerStatSet, SoftReference<LeaderboardFetchResult>> fetchResults = new ConcurrentHashMap<>();
  public static Map<UUID, Map<PlayerStatSet, Integer>> playerRanks = new ConcurrentHashMap<>();

  public static int getPlayerRank(OfflinePlayer player, PlayerStatSet statSet) {
    return playerRanks.get(player.getUniqueId()).get(statSet);
  }

  public static @Nullable PlayerProperties getPlayerAtPos(PlayerStatSet statSet, int rank) {
    final SoftReference<LeaderboardFetchResult> reference = fetchResults.get(statSet);

    if (reference == null)
      return null;

    final LeaderboardFetchResult result = reference.get();

    if (result == null || result.getMaxRank() < rank)
      return null;

    return result.getPropertiesAtRank(rank);
  }

  // Cache the new player
  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    // Add temporary dummy data (Until next re-cache)
    if (!playerRanks.containsKey(event.getPlayer().getUniqueId())) {
      final int playerRank = playerRanks.size() + 1; // Make em last place, they are new

      final Map<PlayerStatSet, Integer> setRanks = new HashMap<>();

      for (PlayerStatSet statSet : PlayerDataAPI.get().getRegisteredStatSets())
        setRanks.put(statSet, playerRank);

      playerRanks.put(event.getPlayer().getUniqueId(), setRanks);
    }
  }

  public static void startAsyncCaching() {
    if (cacheTask != null)
      return;

    cacheTask = Bukkit.getScheduler().runTaskTimerAsynchronously(LeaderboardsPlugin.getInstance(), () -> {
      // Cache leaderboard ranking results
      {
        for (PlayerStatSet stats : PlayerDataAPI.get().getRegisteredStatSets()) {
          PlayerDataAPI.get().fetchLeaderboard(stats, 1, 20, result -> {
            fetchResults.put(stats, new SoftReference<>(result));
          });
        }
      }

      // Cache all player standings
      {
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
          for (PlayerStatSet statSet : PlayerDataAPI.get().getRegisteredStatSets()) {
            PlayerDataAPI.get().fetchLeaderboardPosition(player, statSet, position -> {
              playerRanks.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>()).put(statSet, position);
            });
          }
        }
      }
    }, 0L, 20 * 60L * 10); // Cache every 10 min
  }
}
