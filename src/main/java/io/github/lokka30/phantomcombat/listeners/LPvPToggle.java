package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class LPvPToggle implements Listener {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        if (instance.settings.get("pvp-toggle.enable", true)) {
            if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
                final Player defender = (Player) e.getEntity();
                final Player attacker = (Player) e.getDamager();

                if (!instance.data.get("players." + defender.getUniqueId().toString() + ".pvp", true)) {
                    attacker.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.protected-target", "%target% has PvP disabled").replaceAll("%target%", defender.getDisplayName())));
                    e.setCancelled(true);
                }

                if (!instance.data.get("players." + attacker.getUniqueId().toString() + ".pvp", true)) {
                    attacker.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.protected-you", "You have PvP disabled")));
                    e.setCancelled(true);
                }
            }
        }
    }
}
