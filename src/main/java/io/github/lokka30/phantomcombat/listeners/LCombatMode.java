package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.CombatCause;
import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LCombatMode implements Listener {

    HashMap<UUID, Integer> combatMap = new HashMap<>();
    ArrayList<UUID> cancel = new ArrayList<>();
    private PhantomCombat instance = PhantomCombat.getInstance();

    @EventHandler
    public void onDamageByEntity(final EntityDamageByEntityEvent e) {
        if (instance.settings.getBoolean("combat-mode.enable") && !e.isCancelled()) {
            List<String> enabledWorlds = instance.settings.getStringList("combat-mode.enabled-worlds");
            List<String> enabledGameModes = instance.settings.getStringList("combat-mode.enabled-gamemodes");
            boolean combatCausePlayer = instance.settings.getBoolean("combat-mode.enabled-combat-causes.player");
            boolean combatCauseEntity = instance.settings.getBoolean("combat-mode.enabled-combat-causes.entity");

            if (e.getEntity() instanceof Player) {
                final Player defender = (Player) e.getEntity();
                if (enabledWorlds.contains(defender.getWorld().getName())) {
                    if (enabledGameModes.contains(defender.getGameMode().toString())) {
                        if (e.getDamager() instanceof Player) {
                            //Combat Cause: Player
                            if (combatCausePlayer) {
                                final Player attacker = (Player) e.getDamager();
                                enterCombat(attacker, CombatCause.PLAYER, defender.getName());
                                enterCombat(defender, CombatCause.PLAYER, attacker.getName());
                            }
                        } else {
                            //Combat Cause: Entity
                            if (combatCauseEntity) {
                                for (String entityTypeString : instance.settings.get("combat-mode.blacklisted-entities", Collections.singletonList("ARMOR_STAND, PAINTING, ITEM_FRAME"))) {
                                    if (e.getDamager().getType().name().equalsIgnoreCase(entityTypeString)) {
                                        return;
                                    }
                                }
                                enterCombat(defender, CombatCause.ENTITY, e.getDamager().getType().toString());
                            }
                        }
                    }
                }
            } else if (e.getDamager() instanceof Player) {
                final Player attacker = (Player) e.getDamager();
                if (enabledWorlds.contains(attacker.getWorld().getName())) {
                    if (enabledGameModes.contains(attacker.getGameMode().toString())) {
                        if (combatCauseEntity) {
                            for (String entityTypeString : instance.settings.get("combat-mode.blacklisted-entities", Collections.singletonList("ARMOR_STAND, PAINTING, ITEM_FRAME"))) {
                                if (e.getEntity().getType().name().equalsIgnoreCase(entityTypeString)) {
                                    return;
                                }
                            }
                            enterCombat(attacker, CombatCause.ENTITY, e.getEntity().getType().toString());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent e) {
        final EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK || cause == EntityDamageEvent.DamageCause.SUICIDE || e.isCancelled()) {
            return;
        }
        if (instance.settings.getBoolean("combat-mode.enable")) {
            if (e.getEntity() instanceof Player) {
                final Player defender = (Player) e.getEntity();

                if (instance.settings.getStringList("combat-mode.enabled-worlds").contains(defender.getWorld().getName())) {
                    if (instance.settings.getStringList("combat-mode.enabled-gamemodes").contains(defender.getGameMode().toString())) {
                        if (instance.settings.getBoolean("combat-mode.enabled-combat-causes.generic")) {
                            enterCombat(defender, CombatCause.GENERIC, "none");
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLeave(final PlayerQuitEvent e) {
        final Player p = e.getPlayer();
        final UUID uuid = p.getUniqueId();

        if (combatMap.containsKey(uuid)) {
            if (instance.settings.getBoolean("combat-mode.effects.combat-log.kill-player")) {
                p.setHealth(0.0D);
            }
            if (instance.settings.getBoolean("combat-mode.effects.combat-log.lightning-strike-on-quit")) {
                p.getWorld().strikeLightningEffect(p.getLocation());
            }

            Bukkit.broadcastMessage(instance.colorize(instance.messages.getString("combat-mode.player-left-in-combat").replaceAll("%player%", p.getName())));

            if (instance.settings.getBoolean("combat-mode.effects.combat-log.broadcast-location")) {
                Bukkit.broadcastMessage(instance.colorize(instance.messages.getString("combat-mode.player-left-in-combat-location")
                        .replaceAll("%x%", p.getLocation().getBlockX() + "")
                        .replaceAll("%y%", p.getLocation().getBlockY() + "")
                        .replaceAll("%z%", p.getLocation().getBlockZ() + "")
                        .replaceAll("%world%", Objects.requireNonNull(p.getLocation().getWorld()).getName())));
            }
        }
    }

    @EventHandler
    public void onToggleFlight(final PlayerToggleFlightEvent e) {
        final Player p = e.getPlayer();
        if (instance.settings.getBoolean("combat-mode.block-flight")) {
            if (combatMap.containsKey(p.getUniqueId())) {
                p.setFlying(false);
                p.setAllowFlight(false);
                p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.flight-blocked")));
            }
        }
    }

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent e) {
        final Player p = e.getPlayer();
        if (instance.settings.getBoolean("combat-mode.block-teleport")) {
            if (combatMap.containsKey(p.getUniqueId())) {
                e.setCancelled(true);
                p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.teleport-blocked")));
            }
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (instance.settings.getBoolean("combat-mode.block-items.enable")) {
            if (combatMap.containsKey(p.getUniqueId())) {
                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    final Material mainHand = p.getInventory().getItemInMainHand().getType();
                    final Material offHand = p.getInventory().getItemInOffHand().getType();

                    List<String> blockedMaterials = instance.settings.getStringList("combat-mode.block-items.items");

                    if (blockedMaterials.contains(mainHand.toString()) || blockedMaterials.contains(offHand.toString())) {
                        e.setCancelled(true);
                        p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.item-blocked")));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCommandPreProcess(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        if (instance.settings.getBoolean("combat-mode.block-commands.enable")) {
            if (combatMap.containsKey(p.getUniqueId())) {
                final String[] args = e.getMessage().split(" ");
                final String message = args[0];
                List<String> blockedCommands = instance.settings.getStringList("combat-mode.block-commands.commands");

                for (String blockedCommand : blockedCommands) {
                    if (message.equalsIgnoreCase(blockedCommand)) {
                        e.setCancelled(true);
                        p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.command-blocked")));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(final PlayerDeathEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        if (combatMap.containsKey(uuid)) {
            cancel.add(uuid);
        }
    }

    public void enterCombat(final Player p, CombatCause cause, String extraInfo) {
        final int time = instance.settings.getInt("combat-mode.time");
        final UUID uuid = p.getUniqueId();

        if (combatMap.containsKey(uuid)) {
            //Since the user is already in combat, reset their timer.
            combatMap.replace(uuid, time);
        } else {
            //Put the user into combat mode.
            combatMap.put(uuid, time);

            //Communcating the combat status
            final boolean useActionBar = instance.messages.getBoolean("combat-mode.status.action-bar.enable");
            final boolean useBossBar = instance.messages.getBoolean("combat-mode.status.boss-bar.enable");
            final boolean useChat = instance.messages.getBoolean("combat-mode.status.chat.enable");

            //Setting reason for combat mode
            String reason = getReason(cause, extraInfo);

            //Boss bar args
            final String bossBarPath = "combat-mode.status.boss-bar.";
            String barTitle = instance.colorize(instance.messages.getString(bossBarPath + "counter"));
            BarColor barColor = BarColor.valueOf(instance.messages.getString(bossBarPath + "barColor"));
            BarStyle barStyle = BarStyle.valueOf(instance.messages.getString(bossBarPath + "barStyle"));
            BossBar bossBar = Bukkit.createBossBar(barTitle, barColor, barStyle);

            combatStarted(p, bossBar, useBossBar, useChat, useActionBar, time, reason);

            //Create the Combat Mode task.
            new BukkitRunnable() {
                public void run() {
                    if (cancel.contains(uuid)) {
                        //the combat mode can be forced to expire, e.g. from death.
                        cancel();
                        combatMap.remove(uuid);
                        combatFinished(p, bossBar, useBossBar, useChat, useActionBar);
                        cancel.remove(uuid);
                        return;
                    }

                    //Current Time
                    int current = combatMap.get(uuid);
                    current--;
                    combatMap.replace(uuid, current);

                    if (useBossBar) {
                        //Update the boss bar %time%
                        String plural = "";
                        if (time != 1) {
                            plural = "s";
                        }
                        bossBar.setTitle(instance.colorize(instance.messages.getString(bossBarPath + "counter").replaceAll("%time%", String.valueOf(current)).replaceAll("%s%", plural)));
                    }

                    //If the timer is complete, cancel.
                    if (current < 0) {
                        cancel();
                        combatMap.remove(uuid);
                        cancel.remove(uuid);
                        combatFinished(p, bossBar, useBossBar, useChat, useActionBar);
                    }
                }
            }.runTaskTimer(instance, 0, 20);
        }
    }

    public void combatStarted(Player p, BossBar bossBar, boolean useBossBar, boolean useChat, boolean useActionBar, int time, String reason) {
        if (useBossBar) {
            bossBar.addPlayer(p);
        }
        if (useChat) {
            p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.status.chat.combat-entered")
                    .replaceAll("%time%", String.valueOf(time)).replaceAll("%reason%", reason)));
        }
        if (useActionBar) {
            instance.actionBar(p, instance.messages.getString("combat-mode.status.action-bar.combat-entered")
                    .replaceAll("%time%", String.valueOf(time))
                    .replaceAll("%reason%", reason));
        }
        if (instance.settings.getBoolean("combat-mode.effects.combat-started.enable")) {
            final String path = "combat-mode.effects.combat-started.";
            final Sound sound = Sound.valueOf(instance.settings.getString(path + "sound"));
            final float volume = instance.settings.getFloat(path + "volume");
            final float pitch = instance.settings.getFloat(path + "pitch");

            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    public void combatFinished(Player p, BossBar bossBar, boolean useBossBar, boolean useChat, boolean useActionBar) {
        bossBar.removePlayer(p);
        if (useBossBar) {
            bossBar.removePlayer(p);
        }
        if (useChat) {
            p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.status.chat.combat-expired")));
        }
        if (useActionBar) {
            instance.actionBar(p, instance.messages.getString("combat-mode.status.action-bar.combat-expired"));
        }
        if (instance.settings.getBoolean("combat-mode.effects.combat-finished.enable")) {
            final String path = "combat-mode.effects.combat-finished.";
            final Sound sound = Sound.valueOf(instance.settings.getString(path + "sound"));
            final float volume = instance.settings.getFloat(path + "volume");
            final float pitch = instance.settings.getFloat(path + "pitch");

            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    public String getReason(CombatCause cause, String extraInfo) {
        String reason = "unknown reason";
        switch (cause) {
            case PLAYER:
                reason = instance.messages.getString("combat-mode.status.reasons.player").replaceAll("%player%", extraInfo);
                break;
            case ENTITY:
                reason = instance.messages.getString("combat-mode.status.reasons.entity").replaceAll("%name%", extraInfo);
                break;
            case GENERIC:
                reason = instance.messages.getString("combat-mode.status.reasons.generic");
                break;
            default:
                break;
        }
        return reason;
    }
}
