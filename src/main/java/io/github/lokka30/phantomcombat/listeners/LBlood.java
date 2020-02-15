package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Arrays;

public class LBlood implements Listener {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (instance.settings.getBoolean("blood.enable") && !e.isCancelled()) {
            final Entity entity = e.getEntity();
            if (instance.settings.getStringList("blood.enabled-worlds").contains(entity.getWorld().getName())) {
                if (entity instanceof Player) {
                    if (instance.settings.getBoolean("blood.players-bleed")) {
                        bloodParticles(entity, e.getCause());
                    }
                } else {
                    if (instance.settings.getBoolean("blood.entities-bleed")) {
                        for (String blockedEntityType : instance.settings.get("blood.blacklisted-entities", Arrays.asList("ARMOR_STAND"))) {
                            if (entity.getType().name().equalsIgnoreCase(blockedEntityType)) {
                                return;
                            }
                        }
                        bloodParticles(entity, e.getCause());
                    }
                }
            }
        }
    }

    public void bloodParticles(final Entity entity, final EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case FALL:
                if (!instance.settings.getBoolean("blood.cause.fall")) {
                    return;
                }
                break;
            case DROWNING:
                if (!instance.settings.getBoolean("blood.cause.drowning")) {
                    return;
                }
                break;
            case STARVATION:
                if (!instance.settings.getBoolean("blood.cause.starvation")) {
                    return;
                }
                break;
            case SUFFOCATION:
                if (!instance.settings.getBoolean("blood.cause.suffocation")) {
                    return;
                }
                break;
            case CONTACT:
                if (!instance.settings.getBoolean("blood.cause.contact")) {
                    return;
                }
                break;
            case FIRE:
                if (!instance.settings.getBoolean("blood.cause.fire")) {
                    return;
                }
            default:
                break;
        }
        entity.getWorld().playEffect(entity.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
    }
}
