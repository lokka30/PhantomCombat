package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PvPToggleCommand implements CommandExecutor {

    private PhantomCombat instance;

    public PvPToggleCommand(final PhantomCombat instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        if (instance.settings.get("pvp-toggle.enable", true)) {
            switch (args.length) {
                case 1:
                    if (sender instanceof Player) {
                        final Player p = (Player) sender;
                        final String uuid = p.getUniqueId().toString();

                        if (p.hasPermission("phantomcombat.pvptoggle")) {
                            switch (args[0].toLowerCase()) {
                                case "on":
                                    instance.data.set("players." + uuid + ".pvp", true);
                                    p.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.on", "pvp on")));
                                    break;
                                case "off":
                                    instance.data.set("players." + uuid + ".pvp", false);
                                    p.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.off", "pvp off")));
                                    break;
                                default:
                                    p.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.usage", "invalid usage")));
                                    break;
                            }
                        } else {
                            p.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "no permission")));
                        }
                    } else {
                        sender.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.usage-console", "invalid console usage")));
                    }
                    return true;
                case 2:
                    if (sender instanceof Player && !sender.hasPermission("phantomcombat.pvptoggle.others")) {
                        sender.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "no permission")));
                    } else {
                        final Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(instance.colorize(instance.messages.get("common.target-offline", "target offline")
                                    .replaceAll("%target%", args[1])));
                        } else {
                            final String uuid = target.getUniqueId().toString();

                            switch (args[0].toLowerCase()) {
                                case "on":
                                    instance.data.set("players." + uuid + ".pvp", true);
                                    sender.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.on-target", "pvp on for target")
                                            .replaceAll("%target%", args[1])));
                                    target.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.on", "pvp on")));
                                    break;
                                case "off":
                                    instance.data.set("players." + uuid + ".pvp", false);
                                    sender.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.off-target", "pvp off for target")
                                            .replaceAll("%target%", args[1])));
                                    target.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.off", "pvp off")));
                                    break;
                                default:
                                    sender.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.usage", "invalid usage")));
                                    break;
                            }
                        }
                    }
                    return true;
                default:
                    sender.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.usage", "invalid usage")));
                    return true;
            }
        } else {
            sender.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.module-disabled", "pvp toggle module is disabled")));
        }
        return true;
    }
}
