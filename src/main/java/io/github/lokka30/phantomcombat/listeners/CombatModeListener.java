package io.github.lokka30.phantomcombat.listeners;

import com.moneybags.tempfly.TempFly;
import io.github.lokka30.phantomcombat.PhantomCombat;
import io.github.lokka30.phantomcombat.utils.CombatCause;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

public class CombatModeListener implements Listener {

    private PhantomCombat instance;

    public CombatModeListener(final PhantomCombat instance) {
        this.instance = instance;
    }

    HashMap<UUID, Integer> combatMap = new HashMap<>();
    ArrayList<UUID> cancel = new ArrayList<>();
    ArrayList<Player> wasAllowedFlight = new ArrayList<>();

    @EventHandler
    public void onDamageByEntity(final EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }
        if (instance.settings.get("combat-mode.enable", true) && !e.isCancelled()) {
            List<String> enabledWorlds = instance.settings.get("combat-mode.enabled-worlds", Collections.singletonList("disabledWorld"));
            List<String> enabledGameModes = instance.settings.get("combat-mode.enabled-gamemodes", Arrays.asList("SURVIVAL", "ADVENTURE"));
            boolean combatCausePlayer = instance.settings.get("combat-mode.enabled-combat-causes.player", true);
            boolean combatCauseEntity = instance.settings.get("combat-mode.enabled-combat-causes.entity", true);

            if (e.getEntity() instanceof Player) {
                final Player defender = (Player) e.getEntity();
                if (enabledWorlds.contains(defender.getWorld().getName())) {
                    if (enabledGameModes.contains(defender.getGameMode().toString())) {
                        if (e.getDamager() instanceof Player) {
                            //Combat Cause: Player
                            if (combatCausePlayer) {
                                final Player attacker = (Player) e.getDamager();
                                if (enabledGameModes.contains(attacker.getGameMode().toString())) {
                                    enterCombat(attacker, CombatCause.PLAYER, defender.getName());
                                    enterCombat(defender, CombatCause.PLAYER, attacker.getName());
                                }
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
        if (e.isCancelled()) {
            return;
        }
        final EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK || cause == EntityDamageEvent.DamageCause.SUICIDE || e.isCancelled()) {
            return;
        }
        if (instance.settings.get("combat-mode.enable", true)) {
            if (e.getEntity() instanceof Player) {
                final Player defender = (Player) e.getEntity();

                if (instance.settings.get("combat-mode.enabled-worlds", Collections.singletonList(defender.getWorld().getName())).contains(defender.getWorld().getName())) {
                    if (instance.settings.get("combat-mode.enabled-gamemodes", Arrays.asList("SURVIVAL", "ADVENTURE")).contains(defender.getGameMode().toString())) {
                        if (instance.settings.get("combat-mode.enabled-combat-causes.generic", false)) {
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
            if (instance.settings.get("combat-mode.effects.combat-log.kill-player", true)) {
                p.setHealth(0.0D);
            }

            if (instance.settings.get("combat-mode.effects.combat-log.lightning-strike-on-quit", true)) {
                p.getWorld().strikeLightningEffect(p.getLocation());
            }

            for (String command : instance.settings.get("combat-mode.effects.combat-log.commands-executed-by-console", Collections.singletonList("say %player% combat logged."))) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replaceAll("%player%", p.getName()));
            }

            Bukkit.broadcastMessage(instance.colorize(instance.messages.get("combat-mode.player-left-in-combat", "%player% left in combat").replaceAll("%player%", p.getName())));

            if (instance.settings.get("combat-mode.effects.combat-log.broadcast-location", false)) {
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
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        if (instance.settings.get("combat-mode.block-flight", true)) {
            if (combatMap.containsKey(p.getUniqueId())) {
                p.setFlying(false);
                p.setAllowFlight(false);
                p.sendMessage(instance.colorize(instance.messages.get("combat-mode.flight-blocked", "Flight not allowed in combat")));
            }
        }
    }

    @EventHandler
    public void onSwitchGameMode(final PlayerGameModeChangeEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (!event.isCancelled() && instance.settings.get("combat-mode.block-flight", true)) {
            if (event.getNewGameMode().equals(GameMode.CREATIVE) || event.getNewGameMode().equals(GameMode.SPECTATOR)) {
                if (combatMap.containsKey(uuid)) {
                    if (instance.settings.get("combat-mode.remove-on-gamemode-switch", true)) {
                        cancel.add(uuid);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onTeleport(final PlayerTeleportEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        if (instance.settings.get("combat-mode.block-teleport", false)) {
            if (combatMap.containsKey(p.getUniqueId())) {
                e.setCancelled(true);
                p.sendMessage(instance.colorize(instance.messages.get("combat-mode.teleport-blocked", "teleport blocked in combat")));
            }
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (instance.settings.get("combat-mode.block-items.enable", true)) {
            if (combatMap.containsKey(p.getUniqueId())) {
                if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    final Material mainHand = p.getInventory().getItemInMainHand().getType();
                    final Material offHand = p.getInventory().getItemInOffHand().getType();

                    List<String> blockedMaterials = instance.settings.get("combat-mode.block-items.items", Collections.singletonList("ENDER_PEARL"));

                    if (blockedMaterials.contains(mainHand.toString()) || blockedMaterials.contains(offHand.toString())) {
                        e.setCancelled(true);
                        p.sendMessage(instance.colorize(instance.messages.get("combat-mode.item-blocked", "You can't use that item in combat")));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onCommandPreProcess(final PlayerCommandPreprocessEvent e) {
        if (e.isCancelled()) {
            return;
        }
        final Player p = e.getPlayer();
        if (instance.settings.get("combat-mode.block-commands.enable", true)) {
            if (combatMap.containsKey(p.getUniqueId())) {
                final String[] args = e.getMessage().split(" ");
                final String message = args[0];
                List<String> blockedCommands = instance.settings.get("combat-mode.block-commands.commands", Collections.singletonList("home"));

                for (String blockedCommand : blockedCommands) {
                    if (message.equalsIgnoreCase(blockedCommand)) {
                        e.setCancelled(true);
                        p.sendMessage(instance.colorize(instance.messages.get("combat-mode.command-blocked", "You can't use that command whilst in combat mode")));
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

    public void enterCombat(final Player player, CombatCause cause, String extraInfo) {
        final int time = instance.settings.get("combat-mode.time", 15);
        final UUID uuid = player.getUniqueId();

        if (Bukkit.getPluginManager().getPlugin("TempFly") != null) {
            TempFly.getAPI().toggleTempfly(player, false, false);
        }

        if (combatMap.containsKey(uuid)) {
            //Since the user is already in combat, reset their timer.
            combatMap.replace(uuid, time);
        } else {
            //Put the user into combat mode.
            combatMap.put(uuid, time);

            //Communcating the combat status
            final boolean useActionBar = instance.messages.get("combat-mode.status.action-bar.enable", true);
            final boolean useBossBar = instance.messages.get("combat-mode.status.boss-bar.enable", true);
            final boolean useChat = instance.messages.get("combat-mode.status.chat.enable", true);

            //Setting reason for combat mode
            String reason = getReason(cause, extraInfo);

            //Boss bar args
            final String bossBarPath = "combat-mode.status.boss-bar.";
            String barTitle = instance.colorize(instance.messages.get(bossBarPath + "counter", "Invalid PhantomCombat Config!"));
            BarColor barColor = BarColor.valueOf(instance.messages.get(bossBarPath + "barColor", "BLUE"));
            BarStyle barStyle = BarStyle.valueOf(instance.messages.get(bossBarPath + "barStyle", "SOLID"));
            BossBar bossBar = Bukkit.createBossBar(barTitle, barColor, barStyle);

            combatStarted(player, bossBar, useBossBar, useChat, useActionBar, time, reason);

            if (player.getAllowFlight()) {
                wasAllowedFlight.add(player);
            }

            if (instance.settings.get("combat-mode.block-flight", true)) {
                player.setFlying(false);
                player.setAllowFlight(false);
            }

            //Create the Combat Mode task.
            new BukkitRunnable() {
                public void run() {
                    if (!player.isOnline()) {
                        cancel();
                        combatMap.remove(uuid);
                        cancel.remove(uuid);
                        return;
                    }


                    if (cancel.contains(uuid)) {
                        //the combat mode can be forced to expire, e.g. from death.
                        cancel();
                        if (wasAllowedFlight.contains(player)) {
                            player.setAllowFlight(true);
                        }
                        combatMap.remove(uuid);
                        combatFinished(player, bossBar, useBossBar, useChat, useActionBar);
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
                        bossBar.setTitle(instance.colorize(instance.messages.get(bossBarPath + "counter", "Combat mode %time%").replaceAll("%time%", String.valueOf(current)).replaceAll("%s%", plural)));
                    }

                    //If the timer is complete, cancel.
                    if (current < 0) {
                        cancel();
                        combatMap.remove(uuid);
                        cancel.remove(uuid);
                        combatFinished(player, bossBar, useBossBar, useChat, useActionBar);
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
            p.sendMessage(instance.colorize(instance.messages.get("combat-mode.status.chat.combat-entered", "entered combat mode")
                    .replaceAll("%time%", String.valueOf(time))
                    .replaceAll("%reason%", reason)));
        }
        if (useActionBar) {
            instance.actionBar(p, instance.messages.get("combat-mode.status.action-bar.combat-entered", "entered combat mode")
                    .replaceAll("%time%", String.valueOf(time))
                    .replaceAll("%reason%", reason));
        }
        if (instance.settings.get("combat-mode.effects.combat-started.enable", true)) {
            final String path = "combat-mode.effects.combat-started.";
            final Sound sound = Sound.valueOf(instance.settings.get(path + "sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
            final float volume = instance.settings.get(path + "volume", 1.0F);
            final float pitch = instance.settings.get(path + "pitch", 1.0F);

            p.playSound(p.getLocation(), sound, volume, pitch);
        }
        for (String command : instance.settings.get("combat-mode.commands-on-combat", Collections.singletonList("say %player% entered combat"))) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public void combatFinished(Player p, BossBar bossBar, boolean useBossBar, boolean useChat, boolean useActionBar) {
        bossBar.removePlayer(p);
        if (useBossBar) {
            bossBar.removePlayer(p);
        }
        if (useChat) {
            p.sendMessage(instance.colorize(instance.messages.get("combat-mode.status.chat.combat-expired", "Combat mode expired")));
        }
        if (useActionBar) {
            instance.actionBar(p, instance.messages.get("combat-mode.status.action-bar.combat-expired", "Combat mode expired"));
        }
        if (instance.settings.get("combat-mode.effects.combat-finished.enable", true)) {
            final String path = "combat-mode.effects.combat-finished.";
            final Sound sound = Sound.valueOf(instance.settings.get(path + "sound", "ENTITY_EXPERIENCE_ORB_PICKUP"));
            final float volume = instance.settings.get(path + "volume", 1.0F);
            final float pitch = instance.settings.get(path + "pitch", 1.0F);

            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    public String getReason(CombatCause cause, String extraInfo) {
        String reason = "unknown reason";
        switch (cause) {
            case PLAYER:
                reason = instance.messages.get("combat-mode.status.reasons.player", "%player%").replaceAll("%player%", extraInfo);
                break;
            case ENTITY:
                reason = instance.messages.get("combat-mode.status.reasons.entity", "%name%").replaceAll("%name%", extraInfo);
                break;
            case GENERIC:
                reason = instance.messages.get("combat-mode.status.reasons.generic", "Generic");
                break;
            default:
                break;
        }
        return reason;
    }
}
