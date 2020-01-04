package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CStats implements CommandExecutor {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @Override
    public boolean onCommand(final CommandSender s, final Command cmd, final String label, final String[] args) {
        if (s instanceof Player && !s.hasPermission("phantomcombat.stats")) {
            s.sendMessage(instance.colorize(instance.messages.getString("common.no-permission")));
            return true;
        }

        if (args.length == 0) {
            if (s instanceof Player) {
                getStats(s, (Player) s);
            } else {
                s.sendMessage(instance.colorize(instance.messages.getString("stats.usage-console")));
            }
            return true;
        } else if (args.length == 1) {
            if (s instanceof Player && !s.hasPermission("phantomcombat.stats.others")) {
                s.sendMessage(instance.colorize(instance.messages.getString("common.no-permission")));
                return true;
            }

            final Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                s.sendMessage(instance.colorize(instance.messages.getString("common.target-offline").replaceAll("%target%", args[0])));
                return true;
            }

            getStats(s, target);
            return true;
        } else {
            s.sendMessage(instance.colorize(instance.messages.getString("stats.usage")));
            return true;
        }
    }

    public void getStats(CommandSender browser, Player target) {
        final String uuid = target.getUniqueId().toString();
        final String path = "stats." + uuid + ".";
        final String kills = instance.data.getOrSetDefault(path + "kills", "0");
        final String deaths = instance.data.getOrSetDefault(path + "deaths", "0");
        final String kdr = getKDR(Integer.parseInt(kills), Integer.parseInt(deaths));
        final String killstreak = instance.data.getOrSetDefault(path + "killstreak", "0");
        final String highestKillstreak = instance.data.getOrSetDefault(path + "highestKillstreak", "0");

        List<String> messages = instance.messages.getStringList("stats.list");
        for (String msg : messages) {
            browser.sendMessage(instance.colorize(msg)
                    .replaceAll("%player%", target.getName())
                    .replaceAll("%kills%", kills)
                    .replaceAll("%deaths%", deaths)
                    .replaceAll("%kdr%", kdr)
                    .replaceAll("%killstreak%", killstreak)
                    .replaceAll("%highestKillstreak%", highestKillstreak));
        }
    }

    public String getKDR(int kills, int deaths) {
        if (deaths == 0 && kills != 0) {
            return String.valueOf(kills);
        } else if (deaths == 0 || kills == 0) {
            return "Undetermined";
        } else {
            return String.valueOf(kills / deaths);
        }
    }
}
