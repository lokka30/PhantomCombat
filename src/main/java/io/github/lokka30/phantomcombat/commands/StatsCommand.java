package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

public class StatsCommand implements CommandExecutor {

    private PhantomCombat instance;

    public StatsCommand(final PhantomCombat instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender s, @NotNull final Command cmd, @NotNull final String label, @NotNull final String[] args) {
        if (s instanceof Player && !s.hasPermission("phantomcombat.stats")) {
            s.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "No permission")));
            return true;
        }

        if (args.length == 0) {
            if (s instanceof Player) {
                getStats(s, (Player) s);
            } else {
                s.sendMessage(instance.colorize(instance.messages.get("stats.usage-console", "Invalid usage for console")));
            }
            return true;
        } else if (args.length == 1) {
            if (s instanceof Player && !s.hasPermission("phantomcombat.stats.others")) {
                s.sendMessage(instance.colorize(instance.messages.get("common.no-permission", "No permission")));
                return true;
            }

            final Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                s.sendMessage(instance.colorize(instance.messages.get("common.target-offline", "Target is offline")
                        .replaceAll("%target%", args[0])));
                return true;
            }

            getStats(s, target);
            return true;
        } else {
            s.sendMessage(instance.colorize(instance.messages.get("stats.usage", "Invalid usage")));
            return true;
        }
    }

    public void getStats(CommandSender browser, Player target) {
        final String uuid = target.getUniqueId().toString();
        final String path = "stats." + uuid + ".";
        final String kills = instance.data.getOrSetDefault(path + "kills", "0");
        final String deaths = instance.data.getOrSetDefault(path + "deaths", "0");
        final String kdr = getKDR(Double.parseDouble(kills), Double.parseDouble(deaths));
        final String killstreak = instance.data.getOrSetDefault(path + "killstreak", "0");
        final String highestKillstreak = instance.data.getOrSetDefault(path + "highest-killstreak", "0");

        List<String> messages = instance.messages.get("stats.list", Collections.singletonList("Invalid config!"));
        for (String msg : messages) {
            browser.sendMessage(instance.colorize(msg)
                    .replaceAll("%player%", target.getName())
                    .replaceAll("%kills%", kills)
                    .replaceAll("%deaths%", deaths)
                    .replaceAll("%kdr%", kdr)
                    .replaceAll("%killstreak%", killstreak)
                    .replaceAll("%highest-killstreak%", highestKillstreak));
        }
    }

    public String getKDR(double kills, double deaths) {
        if (deaths == 0 || kills == 0) {
            return "Undetermined";
        } else {
            final DecimalFormat format = new DecimalFormat("#.##");
            return String.valueOf(format.format(kills / deaths));
        }
    }
}
