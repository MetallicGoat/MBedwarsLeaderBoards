package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.player.DefaultPlayerStatSet;
import de.marcely.bedwars.tools.YamlConfigurationDescriptor;
import me.metallicgoat.bedwarsleaderboards.periodicstats.PeriodicStatSetType;
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
  public static String dataLoading = "%placeholderapi_stats_loading%";
  public static boolean periodicStatsEnabled = false;
  public static DayOfWeek resetDay = DayOfWeek.SUNDAY;

  public static List<PeriodicStatSetType> periodicStatsTracked = Arrays.asList(PeriodicStatSetType.values());

  public static List<String> statsTrackedPeriodically = Arrays.asList(
      DefaultPlayerStatSet.WINS.getId(),
      DefaultPlayerStatSet.KILLS.getId(),
      DefaultPlayerStatSet.FINAL_KILLS.getId(),
      DefaultPlayerStatSet.BEDS_DESTROYED.getId(),
      DefaultPlayerStatSet.ROUNDS_PLAYED.getId()
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
    dataLoading = config.getString("data-loading", dataLoading);

    periodicStatsEnabled = config.getBoolean("periodic-stats-enabled", periodicStatsEnabled);

    {
      final String dayOfWeek = config.getString("weekly-reset-day", resetDay.name());

      try {
        resetDay = DayOfWeek.valueOf(dayOfWeek);
      } catch (IllegalArgumentException e) {
        Console.printConfigWarn("'" + dayOfWeek + "' is not a valid day of the week!", "Main");
      }
    }

    {
      final List<PeriodicStatSetType> statTypes = new ArrayList<>();

      if (config.contains("periodic-stats-tracked")) {
        for (String stringType : config.getStringList("periodic-stats-tracked")) {
          try {
            statTypes.add(PeriodicStatSetType.valueOf(stringType.toUpperCase()));
          } catch (IllegalArgumentException e) {
            Console.printConfigWarn("'" + stringType + "' is not a valid periodic stat set type!", "Main");
          }
        }

        periodicStatsTracked = statTypes;
      }
    }

    {
      final List<String> statIds = new ArrayList<>();

      if (config.contains("stats-tracked-periodically")) {
        for (String statId : config.getStringList("stats-tracked-periodically")) {
          if (Util.getStatsSetById(statId) != null)
            statIds.add(statId);
          else
            Console.printConfigWarn("'" + statId + "' is not a registered stat!", "Main");
        }

        statsTrackedPeriodically = statIds;
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
    config.addComment("We recommend you keep this no lower than 5 minutes");
    config.set("re-cache-minutes", Config.reCacheMinutes);

    config.addEmptyLine();

    config.addComment("What should be returned if there is no player at a certian rank.");
    config.addComment("For example if you want the 10th position, but only 5 players have played");
    config.set("unfilled-rank", Config.unfilledRank);

    config.addEmptyLine();

    config.addComment("What should be displayed if a requested placeholder is still in the process of being cached");
    config.set("data-loading", Config.dataLoading);

    config.addEmptyLine();
    config.addEmptyLine();

    config.addComment("======== Periodic Stats ========");

    config.addEmptyLine();

    config.addComment("If some stats should be tracked periodically. (eg. weekly kills stats)");
    config.set("periodic-stats-enabled", periodicStatsEnabled);

    config.addEmptyLine();

    config.addComment("What day of the week 'WEEKLY' stats will reset");
    config.set("weekly-reset-day", resetDay.name());

    config.addEmptyLine();

    {
      final List<String> typeNames = new ArrayList<>();

      for (PeriodicStatSetType type : periodicStatsTracked)
        typeNames.add(type.name());

      config.addComment("Periodic stat types tracked");

      // Display defaults in comment
      for (PeriodicStatSetType type : PeriodicStatSetType.values())
        config.addComment(" - " + type.name());

      config.set("periodic-stats-tracked", typeNames);
    }

    config.addEmptyLine();

    config.addComment("The stats that will actually get tracked");
    config.set("stats-tracked-periodically", statsTrackedPeriodically);

    config.addEmptyLine();

    // save
    getFile(plugin).getParentFile().mkdirs();

    try (Writer writer = Files.newBufferedWriter(getFile(plugin).toPath(), StandardCharsets.UTF_8)) {
      writer.write(config.saveToString());
    }
  }
}
