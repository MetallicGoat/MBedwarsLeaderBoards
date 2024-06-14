package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.arena.picker.ArenaPickerAPI;
import de.marcely.bedwars.api.arena.picker.condition.ArenaConditionGroup;
import de.marcely.bedwars.api.exception.ArenaConditionParseException;
import de.marcely.bedwars.api.player.PlayerStatSet;
import de.marcely.bedwars.tools.YamlConfigurationDescriptor;
import me.metallicgoat.bedwarsleaderboards.periodicstats.CustomTrackedStatSet;
import me.metallicgoat.bedwarsleaderboards.periodicstats.PeriodicType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Config {

  public static long reCacheMinutes = 2;
  public static String unfilledRank = "-";
  public static boolean unfilledRankForZeroValue = true;
  public static String dataLoading = "%placeholderapi_stats_loading%";
  public static boolean customStatsTracking = false;
  public static DayOfWeek resetDay = DayOfWeek.SUNDAY;

  public static List<CustomTrackedStatSet> customStatSets = Arrays.asList(
      buildCustomStatSet("weekly:solos:kills", "Weekly Solos Kills", "bedwars:kills", PeriodicType.WEEKLY, "[players_per_team=1]"),
      buildCustomStatSet("solos:kills", "Solos Kills", "bedwars:kills", null, "[players_per_team=1]"),
      buildCustomStatSet("monthly:wins", "Monthly Wins", "bedwars:wins", PeriodicType.MONTHLY, null)
  );

  private static File getFile(LeaderboardsPlugin plugin) {
    return new File(LeaderboardsPlugin.getAddon().getDataFolder(), "config.yml");
  }

  public static void load(LeaderboardsPlugin plugin) {
    synchronized (Config.class) {
      try {
        loadUnchecked(plugin);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static void loadUnchecked(LeaderboardsPlugin plugin) throws Exception {
    final File file = getFile(plugin);

    if (!file.exists()) {
      save(plugin);
      return;
    }

    // load it
    final FileConfiguration config = new YamlConfiguration();

    try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      config.load(reader);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // read it
    reCacheMinutes = config.getLong("re-cache-minutes", reCacheMinutes);

    unfilledRank = config.getString("unfilled-rank", unfilledRank);
    unfilledRankForZeroValue = config.getBoolean("unfilled-rank-for-zero-value", unfilledRankForZeroValue);
    dataLoading = config.getString("data-loading", dataLoading);

    customStatsTracking = config.getBoolean("custom-stat-tracking-enabled", customStatsTracking);

    {
      final String dayOfWeek = config.getString("weekly-reset-day", resetDay.name());

      try {
        resetDay = DayOfWeek.valueOf(dayOfWeek);
      } catch (IllegalArgumentException e) {
        Console.printConfigWarn("'" + dayOfWeek + "' is not a valid day of the week!", "Main");
      }
    }

    {
      if (config.contains("custom-stat-tracking")) {
        final List<CustomTrackedStatSet> trackedStatSets = new ArrayList<>();

        final ConfigurationSection customStatsSection = config.getConfigurationSection("custom-stat-tracking");

        for (String statId : customStatsSection.getKeys(false)) {
          final ConfigurationSection statSection = customStatsSection.getConfigurationSection(statId);

          final String rawDisplayName = statSection.getString("display-name");
          final String trackedStat = statSection.getString("tracked-stat");
          final String periodicTypeString = statSection.getString("periodic-type");
          final String restriction = statSection.getString("restriction");

          if (rawDisplayName == null) {
            Console.printConfigWarn("Display name is missing! Failed to add custom stat '" + statId + "'", "Main");
            continue;
          }

          if (trackedStat == null) {
            Console.printConfigWarn("The tracked stat is missing! Failed to add custom stat '" + statId + "'", "Main");
            continue;
          }

          PeriodicType periodicType = null;

          if (periodicTypeString != null) {
            try {
              periodicType = PeriodicType.valueOf(periodicTypeString.toUpperCase());
            } catch (IllegalArgumentException ignored) {
              Console.printConfigWarn("'" + periodicTypeString + "' is not a valid periodic type! Failed to add custom stat '" + statId + "'", "Main");
              continue;
            }
          }

          final CustomTrackedStatSet customTrackedStatSet = buildCustomStatSet(statId, rawDisplayName, trackedStat, periodicType, restriction);

          if (customTrackedStatSet != null) {
            trackedStatSets.add(customTrackedStatSet);
          }
        }

        customStatSets = trackedStatSets;
      }
    }

    // auto update file if newer version
    {
      final String currentVersion = config.getString("file-version");

      if (!currentVersion.equals(LeaderboardsPlugin.getInstance().getDescription().getVersion()))
        save(plugin);
    }
  }

  private static void save(LeaderboardsPlugin plugin) throws Exception {
    final YamlConfigurationDescriptor config = new YamlConfigurationDescriptor();

    config.addComment("Used for auto-updating the config file. Ignore it");
    config.set("file-version", LeaderboardsPlugin.getInstance().getDescription().getVersion());

    config.addEmptyLine();

    config.addComment("SETUP GUIDE: https://github.com/MetallicGoat/MBedwarsLeaderBoards");
    config.addComment("SUPPORT: https://discord.gg/57SdFW2E3F");

    config.addEmptyLine();

    config.addComment("PLACEHOLDERS:");
    config.addComment("---> %MBLeaderboards_playeratposition-<statId>-<position>%");
    config.addComment("---> %MBLeaderboards_valueatposition-<statId>-<position>%");
    config.addComment("---> %MBLeaderboards_playerposition-<statId>%");
    config.addComment("---> %MBLeaderboards_playerstat-<statId>%");

    config.addEmptyLine();
    config.addEmptyLine();
    config.addEmptyLine();

    config.addComment("======== Leaderboards Caching ========");

    config.addEmptyLine();

    config.addComment("How many minutes before rankings will automatically be recalculated.");
    config.addComment("We recommend you keep this no lower than 5 minutes.");
    config.set("re-cache-minutes", Config.reCacheMinutes);

    config.addEmptyLine();

    config.addComment("What should be returned if there is no player at a certian rank.");
    config.addComment("For example if you want the 10th position, but only 5 players have played.");
    config.set("unfilled-rank", Config.unfilledRank);

    config.addEmptyLine();

    config.addComment("When enabled, ranks will not be assigned/displayed if player has a zero for a certian stat.");
    config.addComment("If disabled, ranks will be assigned based on the order of players who joined the server for the first time.");
    config.set("unfilled-rank-for-zero-value", Config.unfilledRankForZeroValue);

    config.addEmptyLine();

    config.addComment("What should be displayed if a requested placeholder is still in the process of being cached");
    config.set("data-loading", Config.dataLoading);

    config.addEmptyLine();
    config.addEmptyLine();

    config.addComment("======== Custom Stat Tracking ========");

    config.addEmptyLine();

    config.addComment("If some stats should be tracked periodically. (eg. weekly kills stats)");
    config.set("custom-stat-tracking-enabled", customStatsTracking);

    config.addEmptyLine();

    config.addComment("What day of the week 'WEEKLY' stats will reset.");
    config.set("weekly-reset-day", resetDay.name());

    config.addEmptyLine();
    config.addEmptyLine();

    config.addComment("Custom stats MBLeaderboards will track.");
    config.addComment("Example Configuration:");

    config.addEmptyLine();

    config.addComment("custom-stat-tracking:");
    config.addComment("  solo:weekly:kills:                      # The id of the custom stat (Use this as the statId, for in PAPI placeholders above)");
    config.addComment("    display-name: \"Weekly Solo Kills\"     # The display name of the custom stat");
    config.addComment("    tracked-stat: bedwars:kills           # The stat this custom stat is tracking (or listening to)");
    config.addComment("    periodic-type: weekly                 # When the stats should reset (removing this is equivalent to putting 'never') (daily/weekly/monthly/yearly/never)");
    config.addComment("    restriction: [players_per_team=1]     # An arena picker which specifies what arenas this stat will be tracked in (remove this to apply to all arenas)");

    config.addEmptyLine();

    config.addComment("List of default MBedwars stats: https://javadocs.mbedwars.com/de/marcely/bedwars/api/player/DefaultPlayerStatSet.html");
    config.addComment("Read about Arena Pickers: https://s.marcely.de/mbww15");

    config.addEmptyLine();

    config.addComment("NOTE: Custom stat ids can only contain the following characters: a-z, 0-9, :, _\n");

    config.addEmptyLine();

    {
      for (CustomTrackedStatSet customStat : customStatSets) {
        final String path = "custom-stat-tracking." + customStat.getId() + ".";

        config.set(path + "display-name", customStat.getRawDisplayName());
        config.set(path + "tracked-stat", customStat.getTrackedStatSet().getId());

        if (customStat.getPeriodicType() != null)
          config.set(path + "periodic-type", customStat.getPeriodicType().name());

        if (customStat.getStringRestriction() != null)
          config.set(path + "restriction", customStat.getStringRestriction());

        config.addEmptyLine();
      }
    }

    // save
    getFile(plugin).getParentFile().mkdirs();

    try (Writer writer = Files.newBufferedWriter(getFile(plugin).toPath(), StandardCharsets.UTF_8)) {
      writer.write(config.saveToString());
    }
  }

  private static CustomTrackedStatSet buildCustomStatSet(String id, String rawDisplayNameName, String trackedSetId, PeriodicType type, String restrictionString) {
    final PlayerStatSet trackedSet = Util.getStatsSetById(trackedSetId);

    if (trackedSet == null) {
      Console.printConfigWarn("Cannot track '" + trackedSetId + "'. Invalid id", "Main");
      return null;
    }

    final ArenaConditionGroup restriction;

    if (restrictionString != null) {
      try {
        restriction = ArenaPickerAPI.get().parseCondition(restrictionString);
      } catch (ArenaConditionParseException e) {
        Console.printConfigWarn("Invalid arena restriction '" + restrictionString + "'", "Main");
        return null;
      }
    } else {
      restriction = null;
    }

    return new CustomTrackedStatSet(id, rawDisplayNameName, trackedSet, type, restriction, restrictionString);
  }
}
