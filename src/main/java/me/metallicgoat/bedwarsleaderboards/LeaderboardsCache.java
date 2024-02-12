package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.player.LeaderboardFetchResult;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerProperties;
import de.marcely.bedwars.api.player.PlayerStatSet;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LeaderboardsCache implements Listener {
  private static BukkitTask cacheTask;

  public static Map<PlayerStatSet, SoftReference<LeaderboardFetchResult>> fetchResults = new ConcurrentHashMap<>();
  public static Map<UUID, Map<PlayerStatSet, Integer>> playerRanks = new ConcurrentHashMap<>();

  public static @Nullable Integer getPlayerRank(OfflinePlayer player, PlayerStatSet statSet) {
    final UUID uuid = player.getUniqueId();

    // Player is not yet cached
    if (!playerRanks.containsKey(uuid))
      return null;

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
    cachePlayer(event.getPlayer());
  }

  // We don't want him no more
  @EventHandler
  public void onLeave(PlayerQuitEvent event) {
    playerRanks.remove(event.getPlayer().getUniqueId());
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
        for (Player player : Bukkit.getOnlinePlayers()) {
          cachePlayer(player);
        }
      }
    }, 0L, 20 * 60L * 10); // Cache every 10 min
  }

  private static void cachePlayer(Player player){
    for (PlayerStatSet statSet : PlayerDataAPI.get().getRegisteredStatSets()) {
      PlayerDataAPI.get().fetchLeaderboardPosition(player, statSet, position -> {
        playerRanks.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>()).put(statSet, position);
      });
    }
  }
}
