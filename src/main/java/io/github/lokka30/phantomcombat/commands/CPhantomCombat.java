package io.github.lokka30.phantomcombat.commands;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CPhantomCombat implements CommandExecutor {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @Override
    public boolean onCommand(final CommandSender s, final Command cmd, final String label, final String[] args) {
        s.sendMessage(instance.colorize("&8&m+--------------------------------------------+"));
        s.sendMessage(instance.colorize("&7This server is running &a&lPhantomCombat&a v" + instance.getDescription().getVersion() + "&7."));
        s.sendMessage(instance.colorize("&7Developer: &alokka30&8 | &7Enhance your combat experience!"));
        s.sendMessage(instance.colorize("&7Developed for version &a" + instance.getDescription().getAPIVersion() + "&7."));
        s.sendMessage(instance.colorize("&8&m+--------------------------------------------+"));
        s.sendMessage(instance.colorize("&a&lCommands:"));
        s.sendMessage(instance.colorize("&8 - &2/deathcoords [p] &8| &7View your death coordinates"));
        s.sendMessage(instance.colorize("&8 - &2/phantomcombat &8| &7View plugin information"));
        s.sendMessage(instance.colorize("&8 - &2/stats [p] &8| &7View your PvP stats"));
        s.sendMessage(instance.colorize("&8&m+--------------------------------------------+"));
        if (s instanceof Player) {
            final Player p = (Player) s;
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F);
        }
        return true;
    }
}
