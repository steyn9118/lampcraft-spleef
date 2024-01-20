package spleef.spleef;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import spleef.spleef.stats.StatsManager;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    private final static Spleef plugin = Spleef.getPlugin();

    public static Arena getArenaByPlayer(Player player){
        for (Arena arena : plugin.getArenas()){
            if (arena.getPlayers().contains(player)) return arena;
        }
        return null;
    }

    public static Arena getArenaByID(String id){
        for (Arena arena : plugin.getArenas()){
            if (arena.getID().equals(id)) return arena;
        }
        return null;
    }

    public static void setExpTimer(List<Player> players, int current, int max, boolean notify){
        for (Player player : players){
            player.setLevel(current);
            if (max == 0) player.setExp(1);
            else player.setExp((float) current / max);
            if (notify) player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.AMBIENT, 50, 2);
        }
    }

    public static void resetExpTimer(List<Player> players, boolean toFull){
        if (toFull) setExpTimer(players, 0, 0, false);
        else setExpTimer(players, 0, 1, false);
    }

    public static void resetExpTimer(Player player, boolean toFull){
        List<Player> players = new ArrayList<>(1);
        players.add(player);
        if (toFull) setExpTimer(players, 0, 0, false);
        else setExpTimer(players, 0, 1, false);
    }

    public static void replaceCubeOfBlocks(Block center, int sideAddition, Player player){
        int xIncrement;
        if ((center.getX() - 1) - (center.getX() + 1) < 0) xIncrement = 1;
        else xIncrement = -1;
        int zIncrement;
        if ((center.getZ() - 1) - (center.getZ() + 1) < 0) zIncrement = 1;
        else zIncrement = -1;

        for (int x = center.getX() - sideAddition; x <= center.getX() + sideAddition; x += xIncrement){
            for (int y = center.getY() - sideAddition; y <= center.getY() + sideAddition; y++){
                for (int z = center.getZ() - sideAddition; z <= center.getZ() + sideAddition; z += zIncrement){
                    Block replacingBlock = center.getLocation().getWorld().getBlockAt(x, y, z);
                    boolean isSnowBlock = replacingBlock.getType().equals(Material.SNOW_BLOCK);
                    if (!isSnowBlock) continue;
                    if (player != null) StatsManager.updateBlocksBroken(player.getName());
                    replacingBlock.setType(Material.AIR);
                }
            }
        }
    }
}
