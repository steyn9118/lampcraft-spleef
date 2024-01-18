package spleef.spleef.stats;

import org.bukkit.scheduler.BukkitRunnable;
import spleef.spleef.Spleef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatsManager {

    private static final Spleef plugin = Spleef.getPlugin();
    private static final List<PlayerStats> cache = new ArrayList<>(100);

    public static PlayerStats getStatsByName(String playerName){
        for (PlayerStats stats : cache){
            if (stats.getPlayerName().equals(playerName)){
                return stats;
            }
        }
        PlayerStats dbstats = Database.getPlayerStat(playerName);
        if (dbstats != null){
            cache.add(dbstats);
            return dbstats;
        }
        return null;
    }

    public static void updateGames(String playerName){
        if (getStatsByName(playerName) == null){
            cache.add(new PlayerStats(playerName, 0, 0, 0, 0, new Date()));
        }

        PlayerStats stats = getStatsByName(playerName);
        stats.setGames(stats.getGames() + 1);
    }

    public static void updateWins(String playerName){
        if (getStatsByName(playerName) == null){
            cache.add(new PlayerStats(playerName, 0, 0, 0, 0, new Date()));
        }

        PlayerStats stats = getStatsByName(playerName);
        stats.setWins(stats.getWins() + 1);
    }

    public static void updateBlocksBroken(String playerName){
        if (getStatsByName(playerName) == null){
            cache.add(new PlayerStats(playerName, 0, 0, 0, 0, new Date()));
        }

        PlayerStats stats = getStatsByName(playerName);
        stats.setBlocksBroken(stats.getBlocksBroken() + 1);
    }

    public static void updateBoosterCollected(String playerName){
        if (getStatsByName(playerName) == null){
            cache.add(new PlayerStats(playerName, 0, 0, 0, 0, new Date()));
        }

        PlayerStats stats = getStatsByName(playerName);
        stats.setBoosters(stats.getBoosters() + 1);
    }

    public static void removeStat(String playerName){
        if (getStatsByName(playerName) == null) return;
        cache.remove(getStatsByName(playerName));
        Database.removePlayerStat(playerName);
    }

    // TODO Очистка статов
    public void cleanUp(int inactiveDays){
        // Очистка старых рекордов, не находящихся в топах
    }

    public static void startSavingCycle(){
        BukkitRunnable saveToDBCycle = new BukkitRunnable() {
            @Override
            public void run() {
                saveAllToDB();
            }
        };
        saveToDBCycle.runTaskTimerAsynchronously(plugin, 20L * 60 * plugin.getConfig().getInt("DatabaseSavingTimer"), 20L * 60 * plugin.getConfig().getInt("DatabaseSavingTimer"));
    }

    public static void saveAllToDB(){
        for (PlayerStats stat : cache){
            Database.updatePlayerStat(stat);
        }
        // Способ очистки кэша ПОЛНАЯ ХУЙНЯ!!! Очень нужно будет переписать на CacheEntry с датой обновления записи
        cache.clear();
    }
}
