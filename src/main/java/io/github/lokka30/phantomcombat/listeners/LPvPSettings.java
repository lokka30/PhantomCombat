package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class LPvPSettings implements Listener {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        if (instance.settings.get("pvp-settings.no-hit-delay", false)) {
            p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024.0);
        } else {
            p.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4.0);
        }

        p.saveData();
    }
}
