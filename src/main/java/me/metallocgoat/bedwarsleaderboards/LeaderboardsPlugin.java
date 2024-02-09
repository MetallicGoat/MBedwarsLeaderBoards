package me.metallocgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.BedwarsAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class LeaderboardsPlugin extends JavaPlugin {
  public static final int MIN_MBEDWARS_API_VER = 100;
  public static final String MIN_MBEDWARS_VER_NAME = "5.4";

  @Getter
  private static LeaderboardsPlugin instance;
  @Getter
  private static LeaderboardsAddon addon;

  @Override
  public void onEnable() {
    instance = this;

    if (!checkMBedwars())
      return;
    if (!registerAddon())
      return;

    final PluginDescriptionFile pdf = this.getDescription();

    Console.printInfo(
        "------------------------------",
        pdf.getName() + " For MBedwars",
        "By: " + pdf.getAuthors(),
        "Version: " + pdf.getVersion(),
        "------------------------------"
    );

    BedwarsAPI.onReady(() -> {
      LeaderboardsCache.startAsyncCaching();
      (new Placeholders(this)).register();
    });
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
