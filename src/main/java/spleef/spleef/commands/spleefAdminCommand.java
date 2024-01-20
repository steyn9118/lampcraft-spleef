package spleef.spleef.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import spleef.spleef.Arena;
import spleef.spleef.Spleef;

public class spleefAdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("spleefadmin")){
            if (args.length == 0){
                return false;
            }

            if (args.length == 1){
                if (args[0].equalsIgnoreCase("reload")){
                    Spleef.LoadArenasFromConfig();
                    return false;
                }
                if (args[0].equalsIgnoreCase("players")){
                    for (Arena arena : Spleef.arenas){
                        sender.sendMessage(Component.text("Arena: " + arena.getID()));
                        for (Player player : arena.getPlayers()){
                            sender.sendMessage(Component.text("Player: " + player.getName() + " isSpectator: " + arena.getSpectators().contains(player)));
                        }
                    }
                }
            }
        }
        return false;
    }
}
