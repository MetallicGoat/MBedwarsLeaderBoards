package me.metallocgoat.bedwarsleaderboards;

public class Console {

  public static void printError(String error) {
    LeaderboardsPlugin.getInstance().getLogger().severe(error);
  }

  public static void printWarn(String warn) {
    LeaderboardsPlugin.getInstance().getLogger().warning(warn);
  }

  public static void printInfo(String info) {
    LeaderboardsPlugin.getInstance().getLogger().info(info);
  }

  public static void printWarn(String... strings) {
    for (String s : strings)
      printWarn(s);
  }

  public static void printInfo(String... strings) {
    for (String s : strings)
      printInfo(s);
  }

  public static void printConfigWarn(String warn, String config) {
    printWarn("[Config-" + config + "] " + warn);
  }

  public static void printConfigInfo(String info, String config) {
    printInfo("[Config-" + config + "] " + info);
  }
}
