package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import lombok.Getter;
import me.metallicgoat.bedwarsleaderboards.Config;
import me.metallicgoat.bedwarsleaderboards.LeaderboardsPlugin;
import me.metallicgoat.bedwarsleaderboards.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PeriodicStatSet implements PlayerStatSet {

  @Getter
  private static final List<PeriodicStatSet> periodicStatSets = new ArrayList<>();

  @Getter
  private final String id;
  @Getter
  private final PeriodicStatSetType periodicType;
  private final PlayerStatSet originalStatSet;

  public PeriodicStatSet(PeriodicStatSetType periodicType, PlayerStatSet originalStatSet, String id) {
    this.periodicType = periodicType;
    this.originalStatSet = originalStatSet;
    this.id = id;
  }

  @Override
  public Plugin getPlugin() {
    return LeaderboardsPlugin.getInstance();
  }

  @Override
  public String getName(CommandSender sender) {
    return capitalize(periodicType.name()) + " " + originalStatSet.getName(sender);
  }

  @Override
  public String getDisplayedValue(PlayerStats stats) {
    return formatInt(stats.get(this.periodicType.getId(this)).intValue());
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

  private String capitalize(String str) {
    if (str == null || str.isEmpty())
      return str;

    return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
  }

  public static void registerAll() {
    if (!Config.periodicStatsEnabled)
      return;

    for (String statId : Config.statsTrackedPeriodically) {
      final PlayerStatSet statSet = Util.getStatsSetById(statId);

      if (statSet == null || Util.isPeriodicStat(statSet))
        continue;

      for (PeriodicStatSetType type : Config.periodicStatsTracked) {
        final PeriodicStatSet periodicStatSet = new PeriodicStatSet(type, statSet, type.getId(statSet));

        periodicStatSets.add(periodicStatSet);
        PlayerDataAPI.get().registerStatSet(periodicStatSet);
      }
    }
  }
}
