package spleef.spleef.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import spleef.spleef.Arena;
import spleef.spleef.Utils;

public class spleefCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("spleef")){
            switch (args.length){
                case (0):
                    break;
                case (1):
                    if (args[0].equals("leave")){
                        Player player = (Player) sender;
                        Arena arena = Utils.getArenaByPlayer(player);
                        if (arena == null) return false;
                        arena.leave(player);
                        break;
                    }
                case (3):
                    if (args[0].equalsIgnoreCase("join")){

                        Player player = Bukkit.getPlayer(args[1]);
                        if (player == null) return false;
                        Arena arena = Utils.getArenaByID(args[2]);
                        if (arena == null) return false;
                        arena.join(player);
                        break;
                    }
            }
        }
        return false;
    }
}
