package me.metallicgoat.bedwarsleaderboards;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.marcely.bedwars.api.player.LeaderboardFetchResult;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class LeaderboardsCache {
  private BukkitTask cacheTask;

  private final Cache<UUID, Cache<PlayerStatSet, Integer>> playerRanksCache = buildCache();
  private final Cache<PlayerStatSet, Cache<Integer, LeaderboardFetchResult>> fetchResultsCache = buildCache();

  public static LeaderboardsCache init() {
    final LeaderboardsCache cache = new LeaderboardsCache();

    cache.startAsyncCaching();

    return cache;
  }

  // Trys to get a player rank, and caches it if it's not there
  public @Nullable Integer getPlayerRank(OfflinePlayer player, PlayerStatSet statSet) {
    final UUID uuid = player.getUniqueId();
    Cache<PlayerStatSet, Integer> statSetCache = playerRanksCache.getIfPresent(uuid);

    if (statSetCache == null) {
      statSetCache = buildCache();
      playerRanksCache.put(player.getUniqueId(), statSetCache);
    }

    final Integer rank = statSetCache.getIfPresent(statSet);

    if (rank == null) {
      cachePlayerRank(statSetCache, uuid, statSet);
      return null;
    }

    return rank;
  }

  // Trys to get a player rank, and caches it if it's not there
  public @Nullable LeaderboardFetchResult getCachedFetchResult(PlayerStatSet statSet, int rank) {
    Cache<Integer, LeaderboardFetchResult> resultsBlockCache = fetchResultsCache.getIfPresent(statSet);

    if (resultsBlockCache == null) {
      resultsBlockCache = buildCache();
      fetchResultsCache.put(statSet, resultsBlockCache);
    }

    // the position of the data at the block
    int blockPosition = (rank / 10) + 1; // cannot be zero so add 1

    final LeaderboardFetchResult result = resultsBlockCache.getIfPresent(blockPosition);

    if (result == null)
      cacheResultBlock(resultsBlockCache, statSet, blockPosition);

    return result;
  }

  private void startAsyncCaching() {
    if (this.cacheTask != null)
      return;

    // Only re-cache what is currently cached
    this.cacheTask = Bukkit.getScheduler().runTaskTimerAsynchronously(LeaderboardsPlugin.getInstance(), () -> {
      // player standings
      {
        this.playerRanksCache.asMap().forEach((uuid, statCache) -> {
          statCache.asMap().forEach((statSet, integer) -> {
            cachePlayerRank(statCache, uuid, statSet);
          });
        });
      }

      // update fetch results
      {
        this.fetchResultsCache.asMap().forEach((statSet, fetchCache) -> {
          fetchCache.asMap().forEach((blockPos, result) -> {
            cacheResultBlock(fetchCache, statSet, blockPos);
          });
        });
      }
    }, 0L, 20L * 60 * Config.reCacheMinutes);
  }

  private void cachePlayerRank(Cache<PlayerStatSet, Integer> statSetCache, UUID player, PlayerStatSet statSet){
    PlayerDataAPI.get().fetchLeaderboardPosition(player, statSet, position -> {
      statSetCache.put(statSet, position);
    });
  }

  private void cacheResultBlock(Cache<Integer, LeaderboardFetchResult> resultBlockCache, PlayerStatSet statSet, int blockPosition){
    PlayerDataAPI.get().fetchLeaderboard(statSet,  Math.max(1, (blockPosition * 10) - 10), blockPosition * 10, result -> {
      resultBlockCache.put(blockPosition, result);
    });
  }

  private <K, V>  Cache<K, V> buildCache(){
    return CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build();
  }
}
