package spleef.spleef.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import spleef.spleef.Arena;
import spleef.spleef.Utils;

public class playerListeners implements Listener {

    @EventHandler
    public void onPlayerMovementEvent(PlayerMoveEvent event){
        Player player = event.getPlayer();
        Arena arena = Utils.getArenaByPlayer(player);
        if (arena == null) return;
        if (!arena.isGameActive()) return;

        boolean playerIsLowerThenMinY = arena.getMinY() >= player.getLocation().getBlockY();
        if (playerIsLowerThenMinY) arena.death(player);

        boolean isSpectator = arena.getSpectators().contains(player);
        boolean spectatorOutOfBounds = !arena.getSpectatorsArea().contains(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        if (isSpectator && spectatorOutOfBounds) player.teleport(arena.getSpectatorsSpawnLocation());

    }

    @EventHandler
    public void onPlayerLeaveEvent(PlayerQuitEvent event){
        Player player = event.getPlayer();
        Arena arena = Utils.getArenaByPlayer(event.getPlayer());
        if (arena == null) return;
        arena.leave(player);
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Arena arena = Utils.getArenaByPlayer(player);
        if (arena == null) return;

        Block brokenBlock = event.getBlock();
        boolean isBrokenBlockSnow = brokenBlock.getType().equals(Material.SNOW_BLOCK);
        if (!isBrokenBlockSnow) {
            event.setCancelled(true);
            return;
        }
        arena.snowBlockBroken(player);
        
        boolean hasSuperShovel = player.getInventory().getItemInMainHand().getType().equals(Material.GOLDEN_SHOVEL);
        if (!hasSuperShovel) return;
        Utils.replaceCubeOfBlocks(brokenBlock, 1, player);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event){
        boolean isThrownByPlayer = event.getEntity().getShooter() instanceof Player;
        Block hitBlock = event.getHitBlock();
        if(!isThrownByPlayer) return;

        Player player = (Player) event.getEntity().getShooter();
        Arena arena = Utils.getArenaByPlayer(player);
        if (arena == null) return;

        if (hitBlock == null){
            Utils.replaceCubeOfBlocks(player.getWorld().getBlockAt(event.getHitEntity().getLocation()), 1, player);
        } else Utils.replaceCubeOfBlocks(event.getHitBlock(), 1, null);
    }
}
