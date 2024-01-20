package spleef.spleef;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
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
    private final Location spectatorsSpawnLocation;
    private final int minY;
    private final List<int[]> snowLayers;
    private final int blocksForBooster;
    private final BoundingBox spectatorsArea;

    // Технические
    private final HashMap<Player, BossBar> boosterBossbars = new HashMap<>();
    private final BossBar lobbyBossbar = BossBar.bossBar(Component.text("Ждём игроков...").color(NamedTextColor.WHITE), 1f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
    private final Spleef plugin = Spleef.getPlugin();
    private final List<Player> players = new ArrayList<>();
    private final List<Player> alive = new ArrayList<>();
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
    private boolean lobbyTimerStarted;
    private int timesCleared;

    public List<Player> getPlayers(){
        return players;
    }
    public int getMinY(){
        return  minY;
    }
    public String getID(){
        return ID;
    }
    public boolean isGameActive(){
        return gameActive;
    }
    public Location getSpectatorsSpawnLocation(){
        return spectatorsSpawnLocation;
    }
    public BoundingBox getSpectatorsArea(){
        return spectatorsArea;
    }
    public List<Player> getSpectators(){
        return spectators;
    }



    public Arena(String ID, String name, int maxGameLength, int lobbyTimer, int maxPlayers, int minPlayers, List<Location> startLocations,
                 Location lobbyLocation, Location hubLocation, Location spectatorsSpawnLocation, int minY, List<int[]> snowLayers, int blocksForBooster, BoundingBox spectatorsArea) {
        this.ID = ID;
        this.maxGameLength = maxGameLength;
        this.lobbyTimer = lobbyTimer;
        this.maxPlayers = maxPlayers;
        this.minPlayers = minPlayers;
        this.startLocations = startLocations;
        this.lobbyLocation = lobbyLocation;
        this.hubLocation = hubLocation;
        this.spectatorsSpawnLocation = spectatorsSpawnLocation;
        this.minY = minY;
        this.snowLayers = snowLayers;
        this.blocksForBooster = blocksForBooster;
        this.spectatorsArea = spectatorsArea;

        arenaJoin = Component.text("Вы присоединились к арене " + name).color(NamedTextColor.GRAY);
        arenaLeave = Component.text("Вы покинули арену " + name).color(NamedTextColor.GRAY);
    }

    public void join(Player player){
        if (players.contains(player)) return;
        addPlayer(player);

        if (gameActive || players.size() == maxPlayers){
            player.sendMessage(arenaFull);
            becameSpectator(player);
            return;
        }

        player.showBossBar(lobbyBossbar);
        boosterProgress.put(player, 0);
        boosterBossbars.put(player, BossBar.bossBar(Component.text("Блоков до следующего бустера: ").color(NamedTextColor.YELLOW), 0f, BossBar.Color.YELLOW, BossBar.Overlay.PROGRESS));

        if (players.size() < minPlayers || lobbyTimerStarted) return;
        startLobbyTimer();
    }
    public void leave(Player player){
        if (isAlive(player)){
            death(player);
        }
        clearPlayer(player);
        removePlayer(player);
        player.sendMessage(arenaLeave);
    }


    private void addPlayer(Player player){
        players.add(player);
        player.sendMessage(arenaJoin);
        player.teleport(lobbyLocation);
    }
    private void addAlive(Player player){
        alive.add(player);
        player.setGameMode(GameMode.SURVIVAL);
    }
    public void becameSpectator(Player player){
        spectators.add(player);
        player.setGameMode(GameMode.SPECTATOR);
        clearPlayer(player);
    }
    private void clearPlayer(Player player){
        Utils.resetExpTimer(player, false);
        player.getInventory().clear();
        if (gameActive && boosterBossbars.get(player) != null){
            player.hideBossBar(boosterBossbars.get(player));
        } else {
            player.hideBossBar(lobbyBossbar);
        }
    }
    private void removePlayer(Player player){
        player.teleport(hubLocation);
        player.setGameMode(GameMode.ADVENTURE);
        players.remove(player);
        spectators.remove(player);
        alive.remove(player);
        clearPlayer(player);
    }


    public void death(Player player){
        if (!isAlive(player)) return;

        alive.remove(player);
        player.sendMessage(loseMessage);
        player.showTitle(Title.title(loseMessage, Component.text("")));

        for (Player p : players){
            if (p.equals(player)) continue;
            p.sendMessage(Component.text("Игрок " + player.getName() + " выбывает. Осталось игроков: " + alive.size()).color(NamedTextColor.RED));
        }

        if (alive.size() >= 2) becameSpectator(player);
    }
    private void win(Player winner){
        StatsManager.updateWins(winner.getName());
        winner.sendMessage(winMessage);
        winner.showTitle(Title.title(winMessage, Component.text("")));

        for (Player player : players){
            if (player.equals(winner)) continue;
            player.sendMessage(Component.text("Победил игрок " + winner.getName()).color(NamedTextColor.YELLOW));
            player.showTitle(Title.title(Component.text("Победил игрок " + winner.getName()).color(NamedTextColor.YELLOW), Component.text("")));
        }

        removePlayer(winner);
        stopGame();
    }
    private void spawn(List<Player> players){
        int playersTeleported = 0;

        // Лопата
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
        shovel.addEnchantment(Enchantment.DIG_SPEED, 5);
        ItemMeta meta = shovel.getItemMeta();
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        shovel.setItemMeta(meta);

        for (Player player : players) {
            StatsManager.updateGames(player.getName());
            addAlive(player);
            player.teleport(startLocations.get(playersTeleported));
            player.hideBossBar(lobbyBossbar);
            player.showBossBar(boosterBossbars.get(player));
            player.showTitle(Title.title(startGameTitle, startGameSubtitle));
            player.getInventory().setItemInMainHand(shovel);
            if (startLocations.size() > 1) playersTeleported++;
        }
    }



    private void startLobbyTimer(){
        lobbyTimerStarted = true;
        lobbyBossbar.name(Component.text("Игра скоро начнётся!").color(NamedTextColor.WHITE));
        lobbyBossbar.color(BossBar.Color.GREEN);

        BukkitRunnable countdown = new BukkitRunnable() {
            int count = lobbyTimer;

            @Override
            public void run() {
                Utils.setExpTimer(players, count, lobbyTimer, (count <= 5) );

                if (players.size() < minPlayers){
                    lobbyTimerStarted = false;
                    Utils.resetExpTimer(players, true);
                    lobbyBossbar.name(Component.text("Ждём игроков...").color(NamedTextColor.WHITE));
                    lobbyBossbar.color(BossBar.Color.WHITE);
                    this.cancel();
                    return;
                }

                if (count == 0){
                    startGame();
                    lobbyTimerStarted = false;
                    lobbyBossbar.name(Component.text("Ждём игроков...").color(NamedTextColor.WHITE));
                    lobbyBossbar.color(BossBar.Color.WHITE);
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

        spawn(players);

        BukkitRunnable gameCountdown = new BukkitRunnable() {
            int count = maxGameLength;

            @Override
            public void run() {

                Utils.setExpTimer(players, count, maxGameLength, (count < 10) );

                if (count == (maxGameLength / 3) || count == maxGameLength - (maxGameLength / 3)) reduceFloors();
                if (count == 0) clearAllLayers();

                if (alive.size() == 1){
                    win(alive.get(0));
                    this.cancel();
                    return;
                }
                if (alive.size() == 0){
                    stopGame();
                    this.cancel();
                    return;
                }

                if (count > 0) count -= 1;
            }
        };
        gameCountdown.runTaskTimer(Spleef.getPlugin(), 0,20);
    }

    public void stopGame(){
        if (players.size() != 0){
            for (int i = players.size(); i > 0; i--){
                Player player = players.get(0);
                removePlayer(player);
            }
        }

        gameActive = false;
        players.clear();
        alive.clear();
        spectators.clear();
        boosterProgress.clear();
        boosterBossbars.clear();

        for (int[] floor : snowLayers) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fill " + floor[0] + " " + floor[1] + " " + floor[2] + " " + floor[3] + " " + floor[4] + " " + floor[5] + " minecraft:snow_block");
        }
    }



    private void reduceFloors(){
        if (snowLayers.size() == 1) return;

        if (timesCleared == 0){
            for(int i = 0; i < snowLayers.size() / 2; i++){
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fill " + snowLayers.get(i)[0] + " " + snowLayers.get(i)[1] + " " + snowLayers.get(i)[2] + " " + snowLayers.get(i)[3] + " " + snowLayers.get(i)[4] + " " + snowLayers.get(i)[5] + " minecraft:air");
            }
        } else if (timesCleared == 1){
            for(int i = snowLayers.size() / 2; i < snowLayers.size() - 1; i++){
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fill " + snowLayers.get(i)[0] + " " + snowLayers.get(i)[1] + " " + snowLayers.get(i)[2] + " " + snowLayers.get(i)[3] + " " + snowLayers.get(i)[4] + " " + snowLayers.get(i)[5] + " minecraft:air");
            }
        } else {
            return;
        }

        timesCleared++;
    }

    private void clearAllLayers(){
        for(int[] layer : snowLayers){
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "fill " + layer[0] + " " + layer[1] + " " + layer[2] + " " + layer[3] + " " + layer[4] + " " + layer[5] + " minecraft:air");
        }
    }

    public void snowBlockBroken(Player player){
        StatsManager.updateBlocksBroken(player.getName());

        BossBar bossBar = boosterBossbars.get(player);

        if (boosterProgress.get(player) + 1 == blocksForBooster){
            boosterProgress.replace(player, 0);
            bossBar.name(Component.text("Блоков до следующего бустера: " + blocksForBooster).color(NamedTextColor.YELLOW));
            bossBar.progress(0f);
            giveBooster(player);
            return;
        }

        boosterProgress.replace(player, boosterProgress.get(player) + 1);
        bossBar.name(Component.text("Блоков до следующего бустера: " + (blocksForBooster - boosterProgress.get(player))).color(NamedTextColor.YELLOW));
        bossBar.progress((float) (boosterProgress.get(player)) / blocksForBooster);
    }

    private void giveBooster(Player player){
        StatsManager.updateBoosterCollected(player.getName());
        int rnd = new Random().nextInt(100);
        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.AMBIENT, 100, 1);

        // Снежок
        if (rnd >= 50){
            player.sendMessage(Component.text("Вы получили бустер: Очищающий снежок!").color(NamedTextColor.YELLOW));
            player.sendActionBar(Component.text("Бустер: Очищающий снежок!").color(NamedTextColor.YELLOW));
            ItemStack snowball = new ItemStack(Material.SNOWBALL);
            ItemMeta meta = snowball.getItemMeta();
            meta.displayName(Component.text("Очищающий снежок").color(NamedTextColor.YELLOW));
            snowball.setItemMeta(meta);
            player.getInventory().addItem(snowball);
        }
        // Супер-лопата
        else if (rnd >= 15){
            player.sendMessage(Component.text("Вы получили бустер: Супер-лопата!").color(NamedTextColor.YELLOW));
            player.sendActionBar(Component.text("Бустер: Супер-лопата!").color(NamedTextColor.YELLOW));
            ItemStack superShovel = new ItemStack(Material.GOLDEN_SHOVEL);
            superShovel.addEnchantment(Enchantment.DIG_SPEED, 3);
            ItemMeta meta = superShovel.getItemMeta();
            meta.setUnbreakable(true);
            meta.displayName(Component.text("Супер-лопата").color(NamedTextColor.YELLOW));
            superShovel.setItemMeta(meta);
            for (int i = 0; i < player.getInventory().getSize(); i++){
                if (player.getInventory().getItem(i) == null || !player.getInventory().getItem(i).getType().equals(Material.DIAMOND_SHOVEL)) continue;
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
                        if (player.getInventory().getItem(i) == null || !player.getInventory().getItem(i).getType().equals(Material.GOLDEN_SHOVEL)) continue;
                        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL);
                        shovel.addEnchantment(Enchantment.DIG_SPEED, 5);
                        ItemMeta meta = shovel.getItemMeta();
                        meta.setUnbreakable(true);
                        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        shovel.setItemMeta(meta);
                        player.getInventory().setItem(i, shovel);
                        return;
                    }
                }
            };
            shovelTimer.runTaskLater(plugin, 20 * 5);
        }
        // Невидимость
        else {
            player.sendMessage(Component.text("Вы получили бустер: Невидимость!").color(NamedTextColor.YELLOW));
            player.sendActionBar(Component.text("Бустер: Невидимость!").color(NamedTextColor.YELLOW));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20 * 10, 0, false, false));
        }
    }

    private boolean isAlive(Player player){
        return alive.contains(player);
    }
}