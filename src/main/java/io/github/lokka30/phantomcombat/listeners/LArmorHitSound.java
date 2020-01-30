package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class LArmorHitSound implements Listener {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @EventHandler
    public void onDamage(final EntityDamageEvent e) {
        if (instance.settings.getBoolean("armor-hit-sound.enable") && !e.isCancelled()) {
            if (e.getEntity() instanceof Player) {
                final Player p = (Player) e.getEntity();
                if (p.hasPermission("phantomcombat.armor-hit-sound")) {
                    if (p.getInventory().getHelmet() != null
                            || p.getInventory().getChestplate() != null
                            || p.getInventory().getLeggings() != null
                            || p.getInventory().getBoots() != null) {
                        switch (e.getCause()) {
                            case ENTITY_ATTACK:
                                break;
                            case ENTITY_SWEEP_ATTACK:
                                break;
                            default:
                                return;
                        }

                        Sound sound = Sound.valueOf(instance.settings.getString("armor-hit-sound.sound"));
                        float volume = instance.settings.getFloat("armor-hit-sound.volume");
                        float pitch = instance.settings.getFloat("armor-hit-sound.pitch");

                        p.getWorld().playSound(p.getLocation(), sound, volume, pitch);
                    }
                }
            }
        }
    }
}
