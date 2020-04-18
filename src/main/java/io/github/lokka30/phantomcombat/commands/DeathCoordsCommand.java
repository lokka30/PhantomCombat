package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DeathCoordsCommand implements CommandExecutor {

    private PhantomCombat instance;

    public DeathCoordsCommand(final PhantomCombat instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (instance.settings.get("death-coords.enable", true)) {
            if (!sender.hasPermission("phantomcombat.death-coords")) {
                sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "No permission")));
                return true;
            }

            if (args.length == 0) {
                if (sender instanceof Player) {
                    final Player p = (Player) sender;
                    getDeathCoords(sender, p, true);
                } else {
                    sender.sendMessage(instance.colorize(instance.messages.get("death-coords.usage-console", "Invalid usage for console")));
                }
                return true;
            } else if (args.length == 1) {
                if (sender instanceof Player && !sender.hasPermission("phantomcombat.death-coords.others")) {
                    sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "No permission")));
                    return true;
                }

                final Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(instance.colorize(instance.messages.get("common.target-offline", "%target% is offline").replaceAll("%target%", args[0])));
                    return true;
                }

                getDeathCoords(sender, target, false);

                return true;
            } else {
                sender.sendMessage(instance.colorize(instance.messages.get("death-coords.usage", "Invalid usage")));
                return true;
            }
        } else {
            sender.sendMessage(instance.colorize(instance.messages.get("death-coords.disabled", "Death coords is disabled")));
        }
        return true;
    }

    public void getDeathCoords(final CommandSender s, final Player target, final boolean forSelf) {
        final String uuid = target.getUniqueId().toString();
        final String path = "death-coords." + uuid + ".";
        if (instance.data.get(path + "x") == null) {
            if (forSelf) {
                s.sendMessage(instance.colorize(instance.messages.get("death-coords.you-havent-died", "You haven't died yet")));
            } else {
                s.sendMessage(instance.colorize(instance.messages.get("death-coords.target-hasnt-died", "%target% hasn't died yet")
                        .replaceAll("%target%", target.getName())));
            }
        } else {
            final String x = instance.data.getString(path + "x");
            final String y = instance.data.getString(path + "y");
            final String z = instance.data.getString(path + "z");
            final String world = instance.data.getString(path + "world");

            String msg;
            if (forSelf) {
                msg = instance.messages.get("death-coords.you-died-at", "You died at %x%, %y%, %z% in world %world%");
            } else {
                msg = instance.messages.get("death-coords.target-died-at", "%target% died at %x%, %y%, %z% in world %world%")
                        .replaceAll("%target%", target.getName());
            }
            s.sendMessage(instance.colorize(msg
                    .replaceAll("%x%", x)
                    .replaceAll("%y%", y)
                    .replaceAll("%z%", z)
                    .replaceAll("%world%", world)));
        }
    }
}
