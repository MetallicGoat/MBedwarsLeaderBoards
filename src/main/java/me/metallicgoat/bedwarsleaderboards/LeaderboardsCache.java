package me.metallicgoat.bedwarsleaderboards;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.marcely.bedwars.api.player.LeaderboardFetchResult;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import lombok.Getter;
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
    final Integer rank = statSetCacheHolder.getCache().getIfPresent(statSet);

    if (rank == null || statSetCacheHolder.needsReCache())
      fetchAndCachePlayerRank(statSetCacheHolder, uuid, statSet);

    return rank;
  }

  // Trys to get a player rank, and caches it if it's not there
  public @Nullable LeaderboardFetchResult getCachedFetchResult(PlayerStatSet statSet, int rank) {
    if (rank < 1)
      return null;

    final CacheHolder<Integer, LeaderboardFetchResult> resultsBlockCache = getOrBuildCacheHolder(fetchResultsCache, statSet);
    final int blockPosition = (rank / 10) + 1; // start at 1 (groups of 10)
    final LeaderboardFetchResult result = resultsBlockCache.getCache().getIfPresent(blockPosition);

    if (result == null || resultsBlockCache.needsReCache())
      fetchAndCacheResultBlock(resultsBlockCache, statSet, blockPosition);

    return result;
  }

  private void fetchAndCachePlayerRank(CacheHolder<PlayerStatSet, Integer> holder, UUID player, PlayerStatSet statSet) {
    PlayerDataAPI.get().fetchLeaderboardPosition(player, statSet, position -> {
      holder.update(statSet, position);
    });
  }

  private void fetchAndCacheResultBlock(CacheHolder<Integer, LeaderboardFetchResult> holder, PlayerStatSet statSet, int blockPosition) {
    PlayerDataAPI.get().fetchLeaderboard(statSet, Math.max(1, ((blockPosition - 1) * 10)), blockPosition * 10 - 1, result -> {
      holder.update(blockPosition, result);
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

  private static <Key, Value> Cache<Key, Value> buildCache() {
    return CacheBuilder.newBuilder()
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build();
  }

  private static class CacheHolder<Key, Value> {
    @Getter
    private final Cache<Key, Value> cache = buildCache();
    private long lastRefresh = System.currentTimeMillis();

    public void update(Key key, Value value) {
      this.cache.put(key, value);
      this.lastRefresh = System.currentTimeMillis();
    }

    public boolean needsReCache() {
      return ((System.currentTimeMillis() - lastRefresh) / 1000 / 60) > Config.reCacheMinutes;
    }
  }
}
