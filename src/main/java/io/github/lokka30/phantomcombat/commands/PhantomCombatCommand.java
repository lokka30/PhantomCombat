package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PhantomCombatCommand implements CommandExecutor {

    private PhantomCombat instance;

    public PhantomCombatCommand(final PhantomCombat instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(final CommandSender sender, @NotNull final Command command, @NotNull final String label, @NotNull final String[] args) {
        sender.sendMessage(instance.colorize("&8&m+--------------------------------------------+"));
        sender.sendMessage(instance.colorize("&7Running &aPhantomCombat v" + instance.getDescription().getVersion() + "&7."));
        sender.sendMessage(instance.colorize("&7Developer: &alokka30&8 | &7Enhance your combat experience!"));
        sender.sendMessage(instance.colorize("&7Developed for version &a" + instance.getDescription().getAPIVersion() + "&7."));
        sender.sendMessage(instance.colorize("&7Plugin link: &a&nhttps://www.spigotmc.org/resources/%E2%9A%94-phantomcombat-%E2%9A%94-enhance-your-combat-experience.74060/"));
        sender.sendMessage(instance.colorize("&8&m+--------------------------------------------+"));
        sender.sendMessage(instance.colorize("&a&lCommands:"));
        sender.sendMessage(instance.colorize("&8 - &2/phantomcombat &8| &7View plugin information"));
        sender.sendMessage(instance.colorize("&8 - &2/deathcoords <target> &8| &7View your death coordinates"));
        sender.sendMessage(instance.colorize("&8 - &2/graceperiod forceStart <target> &8| &7Force start a grace period"));
        sender.sendMessage(instance.colorize("&8 - &2/pvptoggle <on/off> [target] &8| &7Set a PvP status"));
        sender.sendMessage(instance.colorize("&8 - &2/stats [p] &8| &7View your PvP stats"));
        sender.sendMessage(instance.colorize("&8&m+--------------------------------------------+"));
        if (sender instanceof Player) {
            final Player p = (Player) sender;
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
        }
        return true;
    }
}
