package me.metallocgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.player.LeaderboardFetchResult;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerProperties;
import de.marcely.bedwars.api.player.PlayerStatSet;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LeaderboardsCache implements Listener {
  private static BukkitTask cacheTask;

  public static Map<PlayerStatSet, LeaderboardFetchResult> fetchResults = new HashMap<>();
  public static Map<UUID, Map<PlayerStatSet, Integer>> playerRanks = new HashMap<>();

//  public static int getPlayerRank(OfflinePlayer player, PlayerStatSet statSet) {
//    return playerRanks.get(player.getUniqueId()).get(statSet);
//  }

  public static PlayerProperties getPlayerAtPos(PlayerStatSet statSet, int rank) {
    return fetchResults.get(statSet).getPropertiesAtRank(rank);
  }

  // Cache the new player
  @EventHandler
  public void onJoin(PlayerJoinEvent event){
    // Add temporary dummy data (Until next re-cache)
    if (!playerRanks.containsKey(event.getPlayer().getUniqueId())) {
      final int playerRank = playerRanks.size() + 1; // Make em last place, they are new

      final Map<PlayerStatSet, Integer> setRanks = new HashMap<>();
      for(PlayerStatSet statSet : PlayerDataAPI.get().getRegisteredStatSets())
          setRanks.put(statSet, playerRank);

      playerRanks.put(event.getPlayer().getUniqueId(), setRanks);
    }
  }

  public static void startAsyncCaching() {
    if (cacheTask != null)
      return;

    cacheTask = Bukkit.getScheduler().runTaskTimerAsynchronously(null, () -> {
      // Cache regular results
      {
        final List<CompletableFuture<LeaderboardFetchResult>> futures = new ArrayList<>();
        for (PlayerStatSet stats : PlayerDataAPI.get().getRegisteredStatSets()) {
          final CompletableFuture<LeaderboardFetchResult> future = new CompletableFuture<>();

          PlayerDataAPI.get().fetchLeaderboard(stats, 1, Integer.MAX_VALUE, future::complete);
          futures.add(future);
        }

        final Map<PlayerStatSet, LeaderboardFetchResult> newResults = new HashMap<>();

        for (CompletableFuture<LeaderboardFetchResult> future : futures) {
          final LeaderboardFetchResult result = future.join();
          newResults.put(result.getStatSet(), result);
        }

        fetchResults = newResults;
      }

//      // Cache player standings
//      {
//        final List<CompletableFuture<Integer>> futures = new ArrayList<>();
//
//        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
//          PlayerDataAPI.get().fetchLeaderboardPosition();
//
//          cachePlayer(player.getUniqueId());
//        }
//      }
    }, 0L, 20 * 60L);
  }
}
