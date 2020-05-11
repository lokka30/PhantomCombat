package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class ArmorHitSoundListener implements Listener {

    private PhantomCombat instance;

    public ArmorHitSoundListener(final PhantomCombat instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent e) {
        if (instance.settings.get("armor-hit-sound.enable", true) && !e.isCancelled() && e.getDamage() != 0.00) {
            if (e.getEntity() instanceof Player) {
                final Player p = (Player) e.getEntity();
                if (p.hasPermission("phantomcombat.armor-hit-sound")) {
                    if (p.getInventory().getHelmet() != null
                            || p.getInventory().getChestplate() != null
                            || p.getInventory().getLeggings() != null
                            || p.getInventory().getBoots() != null) {

                        if (e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
                            if (!(instance.hasWorldGuard && instance.worldGuardUtil.isPVPDenied(p))) {
                                Sound sound = Sound.valueOf(instance.settings.get("armor-hit-sound.sound", "ENTITY_BLAZE_HURT"));
                                float volume = instance.settings.get("armor-hit-sound.volume", 1.0F);
                                float pitch = instance.settings.get("armor-hit-sound.pitch", 1.0F);

                                p.getWorld().playSound(p.getLocation(), sound, volume, pitch);
                            }
                        }
                    }
                }
            }
        }
    }
}
