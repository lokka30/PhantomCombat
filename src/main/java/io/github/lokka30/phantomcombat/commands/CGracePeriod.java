package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import io.github.lokka30.phantomcombat.listeners.LGracePeriod;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CGracePeriod implements CommandExecutor {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @Override
    public boolean onCommand(final CommandSender s, final Command cmd, final String label, final String[] args) {
        if (s instanceof Player && !s.hasPermission("phantomcombat.grace-period")) {
            s.sendMessage(instance.colorize(instance.messages.getString("common.no-permission")));
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("forceStart")) {
                final Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    s.sendMessage(instance.colorize(instance.messages.getString("common.target-offline").replaceAll("%target%", args[1])));
                    return true;
                }

                LGracePeriod.startGracePeriod(target);
                s.sendMessage(instance.colorize(instance.messages.getString("grace-period.force-started").replaceAll("%target%", args[1])));
                return true;
            } else {
                s.sendMessage(instance.colorize(instance.messages.getString("grace-period.usage")));
                return true;
            }
        } else {
            s.sendMessage(instance.colorize(instance.messages.getString("grace-period.usage")));
            return true;
        }
    }
}
