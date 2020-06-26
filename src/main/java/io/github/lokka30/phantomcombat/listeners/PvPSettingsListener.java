package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;

public class PvPSettingsListener implements Listener {

    private PhantomCombat instance;

    public PvPSettingsListener(final PhantomCombat instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        /*

        //This code here is just for debugging purposes for attempting to find out why 1.16 isn't working.

        if(p.getAttribute(Attribute.GENERIC_ATTACK_SPEED) == null) {
            Bukkit.getLogger().severe(ChatColor.RED + "PhantomCombat error: Attribute.GENERIC_ATTACK_SPEED unavailable.");
            for(Attribute attribute : Attribute.values()) {
                if(p.getAttribute(attribute) != null) {
                    Bukkit.getLogger().info(ChatColor.RED + "Attribute " + attribute.name() + " is available.");
                }
            }
            return;
        }*/

        if (instance.settings.get("pvp-settings.no-hit-delay", false)) {
            Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(1024.0);
        } else {
            Objects.requireNonNull(p.getAttribute(Attribute.GENERIC_ATTACK_SPEED)).setBaseValue(4.0);
        }

        p.saveData();
    }
}
