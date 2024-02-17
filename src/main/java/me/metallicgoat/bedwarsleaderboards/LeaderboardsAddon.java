package me.metallicgoat.bedwarsleaderboards;

import de.marcely.bedwars.api.BedwarsAddon;
import org.bukkit.plugin.PluginManager;

public class LeaderboardsAddon extends BedwarsAddon {

  private final LeaderboardsPlugin plugin;

  public LeaderboardsAddon(LeaderboardsPlugin plugin) {
    super(plugin);

    this.plugin = plugin;
  }

  @Override
  public String getName() {
    return plugin.getName();
  }

  public void registerEvents() {
    final PluginManager manager = plugin.getServer().getPluginManager();

    // manager.registerEvents(new LeaderboardsCache(), plugin);
  }
}
