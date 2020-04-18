package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GracePeriodCommand implements CommandExecutor {

    private PhantomCombat instance;

    public GracePeriodCommand(final PhantomCombat instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (!sender.hasPermission("phantomcombat.grace-period")) {
            sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "No permission")));
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("forceStart")) {
                final Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(instance.colorize(instance.messages.get("common.target-offline", "Target offline")
                            .replaceAll("%target%", args[1])));
                    return true;
                }

                instance.gracePeriodListener.startGracePeriod(target);
                sender.sendMessage(instance.colorize(instance.messages.get("grace-period.force-started", "Force started")
                        .replaceAll("%target%", args[1])));
            } else {
                sender.sendMessage(instance.colorize(instance.messages.get("grace-period.usage", "Invalid usage")));
            }
        } else {
            sender.sendMessage(instance.colorize(instance.messages.get("grace-period.usage", "Invalid usage")));
        }
        return true;
    }
}
