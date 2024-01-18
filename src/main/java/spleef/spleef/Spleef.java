package spleef.spleef;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import spleef.spleef.commands.spleefCommand;
import spleef.spleef.commands.spleefAdminCommand;
import spleef.spleef.listeners.playerListeners;
import spleef.spleef.stats.PlaceholderManager;

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

            // Стартовые места
            List<Location> startLocations = (ArrayList<Location>) new ArrayList();
            for (String key : configuration.getConfigurationSection("startLocations").getKeys(false)){
                startLocations.add(configuration.getLocation("startLocations." + key));
            }

            // Слои снега
            List<int[]> floors = new ArrayList();
            for (String key : configuration.getConfigurationSection("floors").getKeys(false)){
                int[] floor = new int[6];
                List<Integer> floorAsList = configuration.getIntegerList("floors." + key);
                for (int i = 0; i < 6; i++){
                    floor[i] = floorAsList.get(i);
                }
                floors.add(floor);
            }

            arenas.add(new Arena(configuration.getString("id"), configuration.getString("name"), configuration.getInt("maxGameLength"),
                    configuration.getInt("lobbyTimer"), configuration.getInt("maxPlayers"), configuration.getInt("minPlayers"), startLocations,
                    configuration.getLocation("lobbyLocation"), configuration.getLocation("hubLocation"), configuration.getInt("minY"), floors,
                    configuration.getInt("blocksForBooster")));

        }

    }

}
