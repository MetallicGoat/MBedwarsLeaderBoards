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

# Placeholders:
Note: <statId> should be replaced with the id of the stat registered with MBedwars.
For example, if we are talking about the kills stat you should replace it with `bedwars:kills`
```
  %MBLeaderboards_playeratposition-<statId>-<position>%
  %MBLeaderboards_playerposition-<statId>%
```

