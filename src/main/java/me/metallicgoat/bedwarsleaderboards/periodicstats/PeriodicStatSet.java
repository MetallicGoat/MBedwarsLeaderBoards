package me.metallicgoat.bedwarsleaderboards.periodicstats;

import de.marcely.bedwars.api.player.PlayerDataAPI;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.api.player.PlayerStats;
import lombok.Getter;
import me.metallicgoat.bedwarsleaderboards.LeaderboardsPlugin;
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
    return capitalizeFirstLetter(capitalizeFirstLetter(periodicType.name()) + " " + this.getName(sender));
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

  private String capitalizeFirstLetter(String input) {
    if (input == null || input.isEmpty())
      return input;

    return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
  }

  public static void registerAll() {
    for (PlayerStatSet statSet : new ArrayList<>(PlayerDataAPI.get().getRegisteredStatSets())) {
      if (isPeriodicStat(statSet))
        continue;

      for (PeriodicStatSetType type : PeriodicStatSetType.values()) {
        final PeriodicStatSet periodicStatSet = new PeriodicStatSet(type, statSet, type.getId(statSet));

        periodicStatSets.add(periodicStatSet);
        PlayerDataAPI.get().registerStatSet(periodicStatSet);
      }
    }
  }

  private static boolean isPeriodicStat(PlayerStatSet statSet) {
    for (PeriodicStatSetType type : PeriodicStatSetType.values())
      if (type.isOfType(statSet.getId()))
        return true;

    return false;
  }
}
