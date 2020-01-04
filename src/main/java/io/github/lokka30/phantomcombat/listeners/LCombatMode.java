package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.CombatCause;
import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class LCombatMode implements Listener {

    HashMap<UUID, Integer> combatMap = new HashMap<>();
    ArrayList<UUID> cancel = new ArrayList<>();
    private PhantomCombat instance = PhantomCombat.getInstance();

    @EventHandler
    public void onDamageByEntity(final EntityDamageByEntityEvent e) {
        if (instance.settings.getBoolean("combat-mode.enable")) {
            if (e.getEntity() instanceof Player) {
                final Player defender = (Player) e.getEntity();

                if (instance.settings.getStringList("combat-mode.enabled-worlds").contains(defender.getWorld().getName())) {
                    if (instance.settings.getStringList("combat-mode.enabled-gamemodes").contains(defender.getGameMode().toString())) {
                        if (e.getDamager() instanceof Player) {
                            if (instance.settings.getBoolean("combat-mode.enabled-combat-causes.player")) {
                                final Player attacker = (Player) e.getDamager();
                                enterCombat(attacker, CombatCause.PLAYER, defender.getName());
                                enterCombat(defender, CombatCause.PLAYER, attacker.getName());
                            }
                        } else {
                            if (instance.settings.getBoolean("combat-mode.enabled-combat-causes.entity")) {
                                enterCombat(defender, CombatCause.ENTITY, e.getDamager().getType().name());
                            }
                        }
                    }
                }
            } else if (e.getDamager() instanceof Player) {
                final Player attacker = (Player) e.getDamager();

                if (instance.settings.getStringList("combat-mode.enabled-worlds").contains(attacker.getWorld().getName())) {
                    if (instance.settings.getStringList("combat-mode.enabled-gamemodes").contains(attacker.getGameMode().toString())) {
                        if (e.getEntity() instanceof Player) {
                            if (instance.settings.getBoolean("combat-mode.enabled-combat-causes.player")) {
                                final Player defender = (Player) e.getEntity();
                                enterCombat(defender, CombatCause.PLAYER, attacker.getName());
                                enterCombat(attacker, CombatCause.PLAYER, defender.getName());
                            }
                        } else {
                            if (instance.settings.getBoolean("combat-mode.enabled-combat-causes.entity")) {
                                enterCombat(attacker, CombatCause.ENTITY, e.getDamager().getType().name());
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(final EntityDamageEvent e) {
        final EntityDamageEvent.DamageCause cause = e.getCause();
        if (cause == EntityDamageEvent.DamageCause.ENTITY_ATTACK || cause == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
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
        if (instance.settings.getBoolean("combat-mode.kill-tagged-player-on-quit")) {
            if (combatMap.containsKey(p.getUniqueId())) {
                p.setHealth(0.0D);
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
    public void onCommand(final PlayerCommandPreprocessEvent e) {
        final Player p = e.getPlayer();
        if (instance.settings.getBoolean("combat-mode.block-commands.enable")) {
            if (combatMap.containsKey(p.getUniqueId())) {
                final String[] args = e.getMessage().split(" ");
                final String message = args[0];

                List<String> blockedCommands = instance.settings.getStringList("combat-mode.block-commands.commands");

                if (blockedCommands.contains(message)) {
                    e.setCancelled(true);
                    p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.command-blocked")));
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
            combatMap.replace(uuid, time);
        } else {
            combatMap.put(uuid, time);

            final boolean useBar = instance.messages.getBoolean("combat-mode.status.bar.enable");
            final boolean useChat = instance.messages.getBoolean("combat-mode.status.chat.enable");

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

            String barTitle = instance.colorize(instance.messages.getString("combat-mode.status.bar.counter"));
            BarColor barColor = BarColor.valueOf(instance.messages.getString("combat-mode.status.bar.barColor"));
            BarStyle barStyle = BarStyle.valueOf(instance.messages.getString("combat-mode.status.bar.barStyle"));
            BossBar bossBar = Bukkit.createBossBar(barTitle, barColor, barStyle);

            if (useBar) {
                bossBar.addPlayer(p);
            }

            if (useChat) {
                p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.status.chat.combat-entered").replaceAll("%time%", String.valueOf(time)).replaceAll("%reason%", reason)));
            }

            new BukkitRunnable() {
                public void run() {
                    if (cancel.contains(uuid)) {
                        //if combat expires e.g. due to death
                        cancel();
                        combatMap.remove(uuid);
                        if (useChat) {
                            p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.status.chat.combat-expired")));
                        }
                        bossBar.removePlayer(p);
                        return;
                    }

                    int current = combatMap.get(uuid);
                    current--;
                    combatMap.replace(uuid, current);

                    if (useBar) {
                        bossBar.setTitle(instance.colorize(instance.messages.getString("combat-mode.status.bar.counter").replaceAll("%time%", String.valueOf(current))));
                    }

                    if (current < 0) {
                        cancel();
                        combatMap.remove(uuid);
                        if (useChat) {
                            p.sendMessage(instance.colorize(instance.messages.getString("combat-mode.status.chat.combat-expired")));
                        }
                        bossBar.removePlayer(p);
                    }
                }
            }.runTaskTimer(instance, 0, 20);
        }
    }
}
