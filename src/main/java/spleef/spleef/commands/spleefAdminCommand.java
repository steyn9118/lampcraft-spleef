package spleef.spleef.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
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
                }
            }
        }
        return false;
    }
}
