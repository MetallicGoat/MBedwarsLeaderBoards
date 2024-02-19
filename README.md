# MBLeaderboards
### PAPI Leaderboards placeholders for MBedwars!

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

# Periodic Stats
MBLeaderboards adds Periodic Stats to MBedwars.
This allows you to track `daily`, `weekly`, `monthly`, and `yearly` stats.
You can also get leaderboards for periodic stats using the above placeholder.
The Stat Id's of Periodic Stats are simple!
Simply take the base stat id of an MBedwars stat, such as and append the id of the periodic stat separated by a colon. 
For example weekly kills would be `weekly:bedwars:kills`, and monthly deaths would be `monthly:bedwars:deaths`.
**NOTE:** you will not be able to use periodic stat id's in the MBedwars placeholders, but you can get a players value using our provided placeholder: `%MBLeaderboards_playerstat-<statId>%`.
