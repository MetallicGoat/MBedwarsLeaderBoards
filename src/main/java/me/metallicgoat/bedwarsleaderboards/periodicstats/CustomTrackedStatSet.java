package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.picker.condition.ArenaConditionGroup;
import de.marcely.bedwars.api.message.Message;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import lombok.Getter;
import me.metallicgoat.bedwarsleaderboards.Config;
import me.metallicgoat.bedwarsleaderboards.LeaderboardsPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CustomTrackedStatSet implements PlayerStatSet {

  @Getter
  private final String id;
  @Getter
  private final PeriodicType periodicType;
  @Getter
  private final PlayerStatSet trackedStatSet;
  @Getter
  private final String stringRestriction;
  @Getter
  private final String rawDisplayName;

  private final ArenaConditionGroup restriction;

  public CustomTrackedStatSet(String id, String displayName, PlayerStatSet trackedStatSet, PeriodicType periodicType, ArenaConditionGroup restriction, String stringRestriction) {
    this.id = id;
    this.rawDisplayName = displayName;
    this.periodicType = periodicType;
    this.trackedStatSet = trackedStatSet;
    this.restriction = restriction;
    this.stringRestriction = stringRestriction;
  }

  @Override
  public Plugin getPlugin() {
    return LeaderboardsPlugin.getInstance();
  }

  @Override
  public String getName(CommandSender sender) {
    return Message.build(this.rawDisplayName).done(sender);
  }

  @Override
  public String getDisplayedValue(PlayerStats stats) {
    return this.trackedStatSet.formatValue(stats.get(this.id));
  }

  @Override
  public String formatValue(Number value) {
    return this.trackedStatSet.formatValue(value);
  }

  @Override
  public Number getValue(PlayerStats playerStats) {
    return playerStats.get(this.id);
  }

  @Override
  public void setValue(PlayerStats stats, Number value) {
    if (!stats.isGameStats()) {
      final Number prev = stats.set(this.id, value);

      stats.getGameStats().add(this.id, value.intValue() - prev.intValue());

    } else
      stats.set(this.id, value);
  }

  public boolean isSupportedInArena(Arena arena) {
    if (arena == null || this.restriction == null)
      return true;

    return this.restriction.check(arena);
  }

  public static void registerAll() {
    if (!Config.customStatsTracking)
      return;

    for (CustomTrackedStatSet customTrackedStatSet : Config.customStatSets)
      PlayerDataAPI.get().registerStatSet(customTrackedStatSet);

  }
}
