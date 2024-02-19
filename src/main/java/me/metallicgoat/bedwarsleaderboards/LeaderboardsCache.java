package me.metallicgoat.bedwarsleaderboards;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.marcely.bedwars.api.player.LeaderboardFetchResult;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class LeaderboardsCache {
  private final Cache<UUID, CacheHolder<PlayerStatSet, Integer>> playerRanksCache = buildCache();
  private final Cache<PlayerStatSet, CacheHolder<Integer, LeaderboardFetchResult>> fetchResultsCache = buildCache();

  // Trys to get a player rank, and caches it if it's not there
  public @Nullable Integer getCachedPlayerRank(UUID uuid, PlayerStatSet statSet) {
    final CacheHolder<PlayerStatSet, Integer> statSetCacheHolder = getOrBuildCacheHolder(playerRanksCache, uuid);
    final boolean needsReCache = statSetCacheHolder.needsReCache();

    final Integer rank = statSetCacheHolder.requestCache().getIfPresent(statSet);

    if (rank == null || needsReCache)
      fetchAndCachePlayerRank(statSetCacheHolder.cache, uuid, statSet);

    return rank;
  }

  // Trys to get a player rank, and caches it if it's not there
  public @Nullable LeaderboardFetchResult getCachedFetchResult(PlayerStatSet statSet, int rank) {
    if (rank < 1)
      return null;

    final CacheHolder<Integer, LeaderboardFetchResult> resultsBlockCache = getOrBuildCacheHolder(fetchResultsCache, statSet);
    final boolean needsReCache = resultsBlockCache.needsReCache();

    // the position of the data at the block
    int blockPosition = (rank / 10) + 1; // cannot be zero so add 1

    final LeaderboardFetchResult result = resultsBlockCache.requestCache().getIfPresent(blockPosition);

    if (result == null || needsReCache)
      fetchAndCacheResultBlock(resultsBlockCache.cache, statSet, blockPosition);

    return result;
  }

  private void fetchAndCachePlayerRank(Cache<PlayerStatSet, Integer> statSetCache, UUID player, PlayerStatSet statSet){
    PlayerDataAPI.get().fetchLeaderboardPosition(player, statSet, position -> {
      statSetCache.put(statSet, position);
    });
  }

  private void fetchAndCacheResultBlock(Cache<Integer, LeaderboardFetchResult> resultBlockCache, PlayerStatSet statSet, int blockPosition){
    PlayerDataAPI.get().fetchLeaderboard(statSet,  Math.max(1, (blockPosition * 10) - 10), blockPosition * 10, result -> {
      resultBlockCache.put(blockPosition, result);
    });
  }

  private <HolderKey, HolderValue, CacheKey> CacheHolder<HolderKey, HolderValue> getOrBuildCacheHolder(Cache<CacheKey, CacheHolder<HolderKey, HolderValue>> outerCache, CacheKey key) {
    CacheHolder<HolderKey, HolderValue> holder = outerCache.getIfPresent(key);

    if (holder == null) {
      holder = new CacheHolder<>();
      outerCache.put(key, holder);
    }

    return holder;
  }

  private static <Key, Value> Cache<Key, Value> buildCache(){
    return CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build();
  }

  private static class CacheHolder<Key, Value> {
    private final Cache<Key, Value> cache = buildCache();
    private long lastAccess = System.currentTimeMillis();

    public boolean needsReCache() {
      return ((System.currentTimeMillis() - lastAccess) / 1000 * 60) > Config.reCacheMinutes;
    }

    public Cache<Key, Value> requestCache() {
      lastAccess = System.currentTimeMillis();
      return cache;
    }
  }
}
