package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class LDeathCoords implements Listener {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @EventHandler
    public void onDeath(final PlayerDeathEvent e) {
        if (instance.settings.getBoolean("death-coords.enable")) {
            final Player p = e.getEntity();
            final String uuid = p.getUniqueId().toString();
            final String path = "death-coords." + uuid + ".";
            instance.data.set(path + "x", String.valueOf(p.getLocation().getBlockX()));
            instance.data.set(path + "y", String.valueOf(p.getLocation().getBlockY()));
            instance.data.set(path + "z", String.valueOf(p.getLocation().getBlockZ()));
            instance.data.set(path + "world", p.getWorld().getName());
            p.sendMessage(instance.colorize(instance.messages.getString("death-coords.on-death")));
        }
    }
}
