package spleef.spleef;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import spleef.spleef.commands.spleefCommand;
import spleef.spleef.commands.spleefAdminCommand;
import spleef.spleef.listeners.playerListeners;
import spleef.spleef.stats.Database;
import spleef.spleef.stats.PlaceholderManager;
import spleef.spleef.stats.StatsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class Spleef extends JavaPlugin {



    private static Spleef plugin;
    public static  ArrayList<Arena> arenas = new ArrayList<>();

    public static Spleef getPlugin(){
        return plugin;
    }

    public ArrayList<Arena> getArenas(){
        return arenas;
    }

    @Override
    public void onEnable() {

        getCommand("spleef").setExecutor(new spleefCommand());
        getCommand("spleefadmin").setExecutor(new spleefAdminCommand());
        Bukkit.getServer().getPluginManager().registerEvents(new playerListeners(), this);
        plugin = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();
        new PlaceholderManager().register();

        Database.initDatabase();
        StatsManager.startSavingCycle();

        LoadArenasFromConfig();
    }

    public static void LoadArenasFromConfig(){

        arenas.clear();
        File arenasFolder = new File(Spleef.getPlugin().getDataFolder() + "/Arenas");
        if (!arenasFolder.exists()){
            arenasFolder.mkdir();
        }
        File[] arenasFiles = arenasFolder.listFiles();

        if (arenasFiles.length == 0){
            return;
        }

        for (File file : arenasFiles){
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);

            List<Location> startLocations = new ArrayList();
            for (String key : configuration.getConfigurationSection("startLocations").getKeys(false)){
                startLocations.add(configuration.getLocation("startLocations." + key));
            }

            List<int[]> snowLayers = new ArrayList();
            for (String key : configuration.getConfigurationSection("snowLayers").getKeys(false)){
                int[] floor = new int[6];
                List<Integer> floorAsList = configuration.getIntegerList("snowLayers." + key);
                for (int i = 0; i < 6; i++){
                    floor[i] = floorAsList.get(i);
                }
                snowLayers.add(floor);
            }

            List<Integer> box = configuration.getIntegerList("spectatorsArea");
            BoundingBox spectatorsArea = new BoundingBox(box.get(0), box.get(1), box.get(2), box.get(3), box.get(4), box.get(5));

            arenas.add(new Arena(configuration.getString("id"),
                    configuration.getString("name"),
                    configuration.getInt("maxGameLength"),
                    configuration.getInt("lobbyTimer"),
                    configuration.getInt("maxPlayers"),
                    configuration.getInt("minPlayers"),
                    startLocations,
                    configuration.getLocation("lobbyLocation"),
                    configuration.getLocation("hubLocation"),
                    configuration.getLocation("spectatorsSpawnLocation"),
                    configuration.getInt("minY"),
                    snowLayers,
                    configuration.getInt("blocksForBooster"), spectatorsArea));
        }
    }
}
