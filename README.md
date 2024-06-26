# MBLeaderboards
### PAPI Leaderboard placeholders for MBedwars!

# Why use this?
Why use this over some other leaderboards plugin?
Well, unlike other leaderboards plugins that works by parsing other PAPI placeholders to figure out what the positions are, 
MBLeaderboards instead pulls the data directly from the MBedwars API, which is much more efficient and less resource intensive.
Also since it is able to pull data directly from the MBedwars API, it does not require parsing possibly hundreds of placeholders to find the data it needs.

# How does it work?
Every time a placeholder is parsed, it will check the MBLeaderboards cache for a value. 
If no cached value is present, MBLeaderboards will use the MBedwars API to asynchronously calculate the value and cache it.
If a cashed value is not referenced for 10 minutes, the cached value is deleted.

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
   solo:weekly:kills:                      # The id of the custom stat (Use this as the statId, for in PAPI placeholders above)
     display-name: "Weekly Solo Kills"     # The display name of the custom stat
     tracked-stat: bedwars:kills           # The stat this custom stat is tracking (or listening to)
     periodic-type: weekly                 # When the stats should reset (removing this is equivalent to putting 'never') (daily/weekly/monthly/yearly/never)
     restriction: [players_per_team=1]     # An arena picker which specifies what arenas this stat will be tracked in (remove this to apply to all arenas)
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