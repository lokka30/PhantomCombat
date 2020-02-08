package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CPvPToggle implements CommandExecutor {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @Override
    public boolean onCommand(final CommandSender s, final Command cmd, final String label, final String[] args) {
        if (instance.settings.get("pvp-toggle.enable", true)) {
            switch (args.length) {
                case 1:
                    if (s instanceof Player) {
                        final Player p = (Player) s;
                        final String uuid = p.getUniqueId().toString();

                        if (p.hasPermission("phantomcombat.pvptoggle")) {
                            switch (args[0].toLowerCase()) {
                                case "on":
                                    instance.data.set("players." + uuid + ".pvp", true);
                                    p.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.on")));
                                    break;
                                case "off":
                                    instance.data.set("players." + uuid + ".pvp", false);
                                    p.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.off")));
                                    break;
                                default:
                                    p.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.usage")));
                                    break;
                            }
                        } else {
                            p.sendMessage(instance.colorize(instance.messages.getString("common.no-permission")));
                        }
                    } else {
                        s.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.usage-console")));
                    }
                    return true;
                case 2:
                    if (s instanceof Player && !s.hasPermission("phantomcombat.pvptoggle.others")) {
                        s.sendMessage(instance.colorize(instance.messages.getString("common.no-permission")));
                    } else {
                        final Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            s.sendMessage(instance.colorize(instance.messages.getString("common.target-offline").replaceAll("%target%", args[1])));
                        } else {
                            final String uuid = target.getUniqueId().toString();

                            switch (args[0].toLowerCase()) {
                                case "on":
                                    instance.data.set("players." + uuid + ".pvp", true);
                                    s.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.on-target").replaceAll("%target%", args[1])));
                                    target.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.on")));
                                    break;
                                case "off":
                                    instance.data.set("players." + uuid + ".pvp", false);
                                    s.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.off-target").replaceAll("%target%", args[1])));
                                    target.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.off")));
                                    break;
                                default:
                                    s.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.usage")));
                                    break;
                            }
                        }
                    }
                    return true;
                default:
                    s.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.usage")));
                    return true;
            }
        } else {
            s.sendMessage(instance.colorize(instance.messages.getString("pvp-toggle.module-disabled")));
        }
        return true;
    }
}
