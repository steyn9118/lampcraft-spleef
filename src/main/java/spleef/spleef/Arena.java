package spleef.spleef;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import spleef.spleef.stats.StatsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class Arena {

    // Из конфига
    private final String ID;
    private final int maxGameLength;
    private final int lobbyTimer;
    private final int maxPlayers;
    private final int minPlayers;
    private final List<Location> startLocations;
    private final Location lobbyLocation;
    private final Location hubLocation;
    private final int minY;
    private final List<int[]> floors;
    private final int blocksForBooster;

    // Технические
    private final HashMap<Player, BossBar> boosterBossbars = new HashMap<>();
    private final Spleef plugin = Spleef.getPlugin();
    private final List<Player> players = new ArrayList<>();
    private final List<Player> spectators = new ArrayList<>();
    private final HashMap<Player, Integer> boosterProgress = new HashMap<>();
    private final Component arenaFull = Component.text("Арена заполнена или игра уже идёт, поэтому вы были присоединены как наблюдатель").color(NamedTextColor.RED);
    private final Component winMessage = Component.text("Вы победили!").color(NamedTextColor.GREEN);
    private final Component loseMessage = Component.text("Вы проиграли!").color(NamedTextColor.RED);
    private final Component startGameTitle = Component.text("Игра началась!").color(NamedTextColor.GREEN);
    private final Component startGameSubtitle = Component.text("Копай снег под другими игроками").color(NamedTextColor.WHITE);
    private final Component arenaJoin;
    private final Component arenaLeave;

    // Динамические
    private boolean gameActive;
    private boolean timerStarted;
    private int timesCleared;

    // Геттеры
    public List<Player> getPlayers(){
        return players;
    }
    public int getMinY(){
        return  minY;
    }
    public String getID(){
        return ID;
    }

    public Arena(String ID, String name, int maxGameLength, int lobbyTimer, int maxPlayers, int minPlayers, List<Location> startLocations,
                 Location lobbyLocation, Location hubLocation, int minY, List<int[]> floors, int blocksForBooster) {
        this.ID = ID;
        this.maxGameLength = maxGameLength;
        this.lobbyTimer = lobbyTimer;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
        this.startLocations = startLocations;
        this.lobbyLocation = lobbyLocation;
        this.hubLocation = hubLocation;
        this.minY = minY;
        this.floors = floors;
        this.blocksForBooster = blocksForBooster;

        arenaJoin = Component.text("Вы присоединились к арене " + name).color(NamedTextColor.GRAY);
        arenaLeave = Component.text("Вы покинули арену " + name).color(NamedTextColor.GRAY);
    }

    public void join(Player player){

        if (gameActive || players.size() == maxPlayers){
            player.sendMessage(arenaFull);
            becameSpectator(player);
            return;
        }

        boosterProgress.put(player, 0);
        boosterBossbars.put(player, BossBar.bossBar(Component.text("Блоков до следующего бустера: ").color(NamedTextColor.YELLOW), 0, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS));
        players.add(player);
        player.sendMessage(arenaJoin);
        player.teleport(lobbyLocation);

        if (players.size() < minPlayers || timerStarted) return;

        startLobbyTimer();
    }

    public void leave(Player player){
        if (!gameActive){
            clearPlayer(player);
            boosterProgress.remove(player);
            boosterBossbars.remove(player);
            player.sendMessage(arenaLeave);
            return;
        }
        death(player);
    }

    public void death(Player player){
        boosterProgress.remove(player);
        boosterBossbars.remove(player);
        player.sendMessage(loseMessage);
        player.showTitle(Title.title(loseMessage, Component.text("")));
        if (players.size() > 2) becameSpectator(player);
        for (Player p : players){
            p.sendMessage(Component.text("Игрок " + player.getName() + " выбывает. Осталось игроков: " + players.size()).color(NamedTextColor.RED));
        }
    }

    private void win(Player player){
        StatsManager.updateWins(player.getName());
        player.sendMessage(winMessage);
        player.showTitle(Title.title(winMessage, Component.text("")));
        stopGame();
    }

    private void startLobbyTimer(){
        timerStarted = true;

        BukkitRunnable countdown = new BukkitRunnable() {
            int count = lobbyTimer;

            @Override
            public void run() {
                Utils.setExpTimer(players, count, lobbyTimer, (count <= 5) );

                if (players.size() < minPlayers){
                    timerStarted = false;
                    Utils.resetExpTimer(players, true);
                    this.cancel();
                    return;
                }

                if (count == 0){
                    startGame();
                    timerStarted = false;
                    this.cancel();
                    return;
                }

                count -= 1;
            }
        };
        countdown.runTaskTimer(Spleef.getPlugin(), 0, 20);
    }

    private void startGame(){
        timesCleared = 0;
        gameActive = true;
        int playersTeleported = 0;
        for (Location loc : startLocations) {
            players.get(playersTeleported).teleport(loc);
            playersTeleported++;
        }

        // Лопаты
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        shovel.addEnchantment(Enchantment.DIG_SPEED, 5);
        ItemMeta meta = shovel.getItemMeta();
        meta.setUnbreakable(true);
        shovel.setItemMeta(meta);
        for (Player player : players){
            StatsManager.updateGames(player.getName());
            player.getInventory().setItemInMainHand(shovel);
            player.showTitle(Title.title(startGameTitle, startGameSubtitle));
            player.setGameMode(GameMode.SURVIVAL);
        }

        BukkitRunnable gameCountdown = new BukkitRunnable() {
            int count = maxGameLength;

            @Override
            public void run() {

                Utils.setExpTimer(players, count, maxGameLength, (count < 10) );
                Utils.setExpTimer(spectators, count, maxGameLength, (count < 10) );

                if (count == (maxGameLength / 3) || count == maxGameLength - (maxGameLength / 3)) reduceFloors();

                if (players.size() == 1){
                    win(players.get(0));
                    this.cancel();
                    return;
                }

                if (count <= 0){
                    stopGame();
                    this.cancel();
                    return;
                }

                count -= 1;
            }
        };
        gameCountdown.runTaskTimer(Spleef.getPlugin(), 0,20);

    }

    public void stopGame(){
        if (players.size() != 0){
            for (Player player : players){
                death(player);
            }
        }

        gameActive = false;
        players.clear();
        boosterProgress.clear();
        spectators.clear();
        boosterBossbars.clear();

        for (int[] floor : floors) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fill " + floor[0] + " " + floor[1] + " " + floor[2] + " " + floor[3] + " " + floor[4] + " " + floor[5] + " minecraft:snow_block");
        }
    }

    public void becameSpectator(Player player){
        spectators.add(player);
        player.setGameMode(GameMode.SPECTATOR);
    }

    private void clearPlayer(Player player){
        boosterProgress.remove(player);
        boosterBossbars.remove(player);
        Utils.resetExpTimer(player, false);
        player.setGameMode(GameMode.ADVENTURE);
        player.teleport(hubLocation);
        player.getInventory().clear();
    }

    private void reduceFloors(){
        if (floors.size() == 1) return;

        if (timesCleared == 0){
            for(int i = 0; i < floors.size() / 2; i++){
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fill " + floors.get(i)[0] + " " + floors.get(i)[1] + " " + floors.get(i)[2] + " " + floors.get(i)[3] + " " + floors.get(i)[4] + " " + floors.get(i)[5] + " minecraft:air");
            }
        } else if (timesCleared == 1){
            for(int i = floors.size() / 2; i < floors.size() - 1; i++){
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fill " + floors.get(i)[0] + " " + floors.get(i)[1] + " " + floors.get(i)[2] + " " + floors.get(i)[3] + " " + floors.get(i)[4] + " " + floors.get(i)[5] + " minecraft:air");
            }
        } else {
            return;
        }

        timesCleared++;
    }

    public void snowBlockBroken(Player player){
        StatsManager.updateBlocksBroken(player.getName());

        boosterBossbars.replace(player, BossBar.bossBar(Component.text("Блоков до следующего бустера: " + boosterProgress.get(player)).color(NamedTextColor.YELLOW),
                boosterProgress.get(player) + 1, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS));

        if (boosterProgress.get(player) + 1 == blocksForBooster){
            boosterProgress.replace(player, 0);
            giveBooster(player);
            return;
        }
        boosterProgress.replace(player, boosterProgress.get(player) + 1);

    }

    private void giveBooster(Player player){
        StatsManager.updateBoosterCollected(player.getName());
        int rnd = new Random().nextInt(100);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 100, 1);

        // Снежок
        if (rnd >= 66){
            ItemStack snowball = new ItemStack(Material.SNOWBALL);
            ItemMeta meta = snowball.getItemMeta();
            meta.displayName(Component.text("Очищающий снежок").color(NamedTextColor.YELLOW));
            snowball.setItemMeta(meta);
            player.getInventory().addItem(snowball);
        }
        // Супер-лопата
        else if (rnd >= 33){
            ItemStack superShovel = new ItemStack(Material.GOLDEN_SHOVEL);
            ItemMeta meta = superShovel.getItemMeta();
            meta.setUnbreakable(true);
            meta.displayName(Component.text("Супер-лопата").color(NamedTextColor.YELLOW));
            superShovel.addEnchantment(Enchantment.DIG_SPEED, 3);
            superShovel.setItemMeta(meta);
            for (int i = 0; i < player.getInventory().getSize(); i++){
                if (player.getInventory().getItem(i) != null && !player.getInventory().getItem(i).getType().equals(Material.DIAMOND_SHOVEL)) continue;
                player.getInventory().setItem(i, superShovel);
                break;
            }
            BukkitRunnable shovelTimer = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!gameActive && !players.contains(player)){
                        this.cancel();
                        return;
                    }

                    for (int i = 0; i < player.getInventory().getSize(); i++){
                        if (player.getInventory().getItem(i) != null && !player.getInventory().getItem(i).getType().equals(Material.GOLDEN_SHOVEL)) continue;
                        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
                        shovel.addEnchantment(Enchantment.DIG_SPEED, 5);
                        ItemMeta meta = shovel.getItemMeta();
                        meta.setUnbreakable(true);
                        shovel.setItemMeta(meta);
                        player.getInventory().setItem(i, shovel);
                        break;
                    }
                }
            };
            shovelTimer.runTaskLater(plugin, 20 * 10);
        }
        // Невидимость
        else {
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 10, 1, false, false));
        }
    }
}