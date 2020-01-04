package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CDeathCoords implements CommandExecutor {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @Override
    public boolean onCommand(final CommandSender s, final Command cmd, final String label, final String[] args) {
        if (instance.settings.getBoolean("death-coords.enable")) {
            if (s instanceof Player && !s.hasPermission("phantomcombat.death-coords")) {
                s.sendMessage(instance.colorize(instance.messages.getString("common.no-permission")));
                return true;
            }

            if (args.length == 0) {
                if (s instanceof Player) {
                    final Player p = (Player) s;
                    getDeathCoords(s, p, true);
                } else {
                    s.sendMessage(instance.colorize(instance.messages.getString("death-coords.usage-console")));
                }
                return true;
            } else if (args.length == 1) {
                if (s instanceof Player && !s.hasPermission("phantomcombat.death-coords.others")) {
                    s.sendMessage(instance.colorize(instance.messages.getString("common.no-permission")));
                    return true;
                }

                final Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    s.sendMessage(instance.colorize(instance.messages.getString("common.target-offline").replaceAll("%target%", args[0])));
                    return true;
                }

                getDeathCoords(s, target, false);

                return true;
            } else {
                s.sendMessage(instance.colorize(instance.messages.getString("death-coords.usage")));
                return true;
            }
        } else {
            s.sendMessage(instance.colorize(instance.messages.getString("death-coords.disabled")));
        }
        return true;
    }

    public void getDeathCoords(final CommandSender s, final Player target, final boolean forSelf) {
        final String uuid = target.getUniqueId().toString();
        final String path = "death-coords." + uuid + ".";
        if (instance.data.get(path + "x") == null) {
            if (forSelf) {
                s.sendMessage(instance.colorize(instance.messages.getString("death-coords.you-havent-died")));
            } else {
                s.sendMessage(instance.colorize(instance.messages.getString("death-coords.target-hasnt-died").replaceAll("%target%", target.getName())));
            }
        } else {
            final String x = instance.data.getString(path + "x");
            final String y = instance.data.getString(path + "y");
            final String z = instance.data.getString(path + "z");
            final String world = instance.data.getString(path + "world");

            String msg;
            if (forSelf) {
                msg = instance.messages.getString("death-coords.you-died-at");
            } else {
                msg = instance.messages.getString("death-coords.target-died-at").replaceAll("%target%", target.getName());
            }
            s.sendMessage(instance.colorize(msg
                    .replaceAll("%x%", x)
                    .replaceAll("%y%", y)
                    .replaceAll("%z%", z)
                    .replaceAll("%world%", world)));
        }
    }
}
