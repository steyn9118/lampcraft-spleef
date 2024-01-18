package spleef.spleef.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import spleef.spleef.Arena;
import spleef.spleef.Utils;

public class playerListeners implements Listener {

    @EventHandler
    public void onPlayerMovementEvent(PlayerMoveEvent event){
        if (Utils.getArenaByPlayer(event.getPlayer()) != null && Utils.getArenaByPlayer(event.getPlayer()).getMinY() >= event.getPlayer().getLocation().getBlockY())
            Utils.getArenaByPlayer(event.getPlayer()).death(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event){
        if (Utils.getArenaByPlayer(event.getPlayer()) == null) return;
        Utils.getArenaByPlayer(event.getPlayer()).leave(event.getPlayer());
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        Arena arena = Utils.getArenaByPlayer(event.getPlayer());
        if (arena == null) return;

        Block brokenBlock = event.getBlock();
        boolean isBrokenBlockSnow = brokenBlock.getType().equals(Material.SNOW_BLOCK);
        if (!isBrokenBlockSnow) {
            event.setCancelled(true);
            return;
        }
        arena.snowBlockBroken(event.getPlayer());

        Player player = event.getPlayer();
        boolean hasSuperShovel = player.getInventory().getItemInMainHand().getType().equals(Material.GOLDEN_SHOVEL);
        if (!hasSuperShovel) return;
        for (int x = brokenBlock.getX() - 1; x <= brokenBlock.getX() + 1; x++){
            for (int y = brokenBlock.getY() - 1; y <= brokenBlock.getY() + 1; y++){
                for (int z = brokenBlock.getZ() - 1; z <= brokenBlock.getZ() + 1; z++){
                    boolean isSnowBlock = player.getLocation().getWorld().getBlockAt(x, y, z).getType().equals(Material.SNOW_BLOCK);
                    if (!isSnowBlock) return;
                    player.getLocation().getWorld().getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
}
