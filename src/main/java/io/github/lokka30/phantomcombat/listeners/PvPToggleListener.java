package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class PvPToggleListener implements Listener {

    private PhantomCombat instance;

    public PvPToggleListener(final PhantomCombat instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (instance.settings.get("pvp-toggle.enable", true)) {
            if (e.getEntity() instanceof Player) {
                if (checkPvPToggle((Player) e.getEntity(), e.getDamager())) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onSplash(final PotionSplashEvent e) {
        if (e.isCancelled()) {
            return;
        }

        for (LivingEntity livingEntity : e.getAffectedEntities()) {
            if (livingEntity instanceof Player) {
                final Player p = (Player) livingEntity;
                if (!(instance.data.get("players." + p.getUniqueId().toString() + ".pvp", true))) {
                    e.getAffectedEntities().remove(livingEntity);
                }
            }
        }
    }

    private boolean checkPvPToggle(Player defender, Entity attackerEntity) {
        final boolean defenderPvP = instance.data.get("players." + defender.getUniqueId().toString() + ".pvp", true);

        if (attackerEntity instanceof Player) {
            final Player attacker = (Player) attackerEntity;

            if (!defenderPvP) {
                attacker.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.protected-target", "%target% has PvP disabled").replaceAll("%target%", defender.getDisplayName())));
                return true;
            }

            if (!instance.data.get("players." + attacker.getUniqueId().toString() + ".pvp", true)) {
                attacker.sendMessage(instance.colorize(instance.messages.get("pvp-toggle.protected-you", "You have PvP disabled")));
                return true;
            }
        } else if (attackerEntity instanceof Arrow) {
            return ((Arrow) attackerEntity).getShooter() instanceof Player && !defenderPvP;
        } else if (attackerEntity instanceof Egg) {
            return ((Egg) attackerEntity).getShooter() instanceof Player && !defenderPvP;
        } else if (attackerEntity instanceof Snowball) {
            return ((Snowball) attackerEntity).getShooter() instanceof Player && !defenderPvP;
        } else if (attackerEntity instanceof Trident) {
            return ((Trident) attackerEntity).getShooter() instanceof Player && !defenderPvP;
        }
        return false;
    }
}
