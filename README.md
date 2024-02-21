# MBLeaderboards
### PAPI Leaderboard placeholders for MBedwars!

# How does it work?
MBLeaderboars asynchronously caches leaderboard standings calculated using MBedwars's built in leaderboards API.
Every 10 minutes all rankings for every statistic are asynchronous recalculated and re-cached so there is very little overhead when a placeholder is being parsed.
When a player joins the game, their rank for every statistic is cached, and when they leave, that cache is deleted.
The top 15 positions of every stat are always cached.

# Dependencies
- MBedwars (Obviously)
- PlaceholderAPI (aka PAPI)

# Placeholders
Note: <statId> should be replaced with the id of the stat registered with MBedwars.
For example, if we are talking about the kills stat you should replace it with `bedwars:kills`
```
  %MBLeaderboards_playeratposition-<statId>-<position>%
  %MBLeaderboards_valueatposition-<statId>-<position>%
  %MBLeaderboards_playerposition-<statId>%
  %MBLeaderboards_playerstat-<statId>%
```

# Custom Stat Tracking
Custom Stat Tracking is a very powerful feature of MBLeaderboards!
MBedwars leaderboards allows you to track existing bedwars stats periodically, and supports arena restrictions!
This means you can track player's weekly kills, wins in solos arenas, or something more complicated like beds destroyed in doubles arenas on a monthly basis!

Here is an example configuration:
```yml
# Custom stats MBLeaderboards will track
custom-stat-tracking:

  solo:weekly:kills:                      # The id of the custom stat (Can be anything, for use in placeholders)
    display-name: "Weekly Solo Kills"     # The display name of the custom stat
    tracked-stat: bedwars:kills           # The regular stat this custom stat is tracking
    periodic-type: weekly                 # When the stats should reset (removing this is equivalent to putting 'never') (daily/weekly/monthly/yearly/never)
    restriction: [players-per-team=1]     # What arenas this stat will be tracked in (remove to apply to all)
```
In this configuration we created a stat to track player kills in solos arenas. 
The stat will reset for everyone every week on the configured weekday!

To get the value of the stat use `%MBLeaderboards_playerstat-solo:weekly:kills%`
To get the player with the most solo kills this week use: `%MBLeaderboards_playeratposition-solo:weekly:kills-1%`

Please note that when using placeholders for the first time with new stats, MBedwars may have to reindex the data. **This should only happen once per new stat**, and is 100% normal. You should see something like this:
```
[21:54:09 INFO]: [MBedwars] (Storage-Local) Indexing all player stats due to missing stat set solos:kills...
[21:54:09 INFO]: [MBedwars] (Storage-Local) Indexed 6 players and 18 stat sets
```

Make sure to enable Custom Stat Tracking in the config.yml! The server will need to be restarted when making configuration changes, and I do not plan on adding a reload system to this addon, so don't bother asking :)