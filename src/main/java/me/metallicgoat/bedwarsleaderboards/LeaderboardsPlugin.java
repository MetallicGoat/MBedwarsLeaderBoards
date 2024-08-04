package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.player.PlayerDataAPI;
import lombok.Getter;
import me.metallicgoat.bedwarsleaderboards.periodicstats.CustomTrackedStatSet;
import me.metallicgoat.bedwarsleaderboards.periodicstats.PeriodicStatResetter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class LeaderboardsPlugin extends JavaPlugin {

  public static final int MIN_MBEDWARS_API_VER = 108;
  public static final String MIN_MBEDWARS_VER_NAME = "5.4.8";

  @Getter
  private static LeaderboardsPlugin instance;
  @Getter
  private static LeaderboardsAddon addon;
  private static Placeholders placeholders;
  private static boolean loaded = false;

  @Override
  public void onEnable() {
    instance = this;

    if (!checkMBedwars())
      return;
    if (!registerAddon())
      return;

    addon.registerEvents();
    addon.test();

    final PluginDescriptionFile pdf = this.getDescription();

    Console.printInfo(
        "------------------------------",
        pdf.getName() + " For MBedwars",
        "By: " + pdf.getAuthors(),
        "Version: " + pdf.getVersion(),
        "------------------------------"
    );

    BedwarsAPI.onReady(() -> {
      if (!loaded) {
        // Load Config before we register all the stats (user might disable some)
        Config.load(this);

        CustomTrackedStatSet.registerAll();
        PeriodicStatResetter.startResettingTask(); // Manages the reset of periodic stats

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
          placeholders = new Placeholders(this);
          placeholders.register();
        } else {
          Console.printWarn("PAPI not installed! PAPI Placeholders will not work!");
        }

        loaded = true;
      }
    });
  }

  @Override
  public void onDisable() {
    for (CustomTrackedStatSet statSet : Config.customStatSets)
      PlayerDataAPI.get().unregisterStatSet(statSet);

    if (placeholders != null)
      placeholders.unregister();
  }

  private boolean checkMBedwars() {
    try {
      final Class<?> apiClass = Class.forName("de.marcely.bedwars.api.BedwarsAPI");
      final int apiVersion = (int) apiClass.getMethod("getAPIVersion").invoke(null);

      if (apiVersion < MIN_MBEDWARS_API_VER)
        throw new IllegalStateException();
    } catch (Exception e) {
      getLogger().warning("Sorry, your installed version of MBedwars is not supported. Please install at least v" + MIN_MBEDWARS_VER_NAME);
      Bukkit.getPluginManager().disablePlugin(this);

      return false;
    }

    return true;
  }

  private boolean registerAddon() {
    addon = new LeaderboardsAddon(this);

    if (!addon.register()) {
      getLogger().warning("It seems like this addon has already been loaded. Please delete duplicates and try again.");
      Bukkit.getPluginManager().disablePlugin(this);

      return false;
    }

    return true;
  }
}
