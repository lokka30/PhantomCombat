package io.github.lokka30.phantomcombat.listeners;

import io.github.lokka30.phantomcombat.PhantomCombat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class LStats implements Listener {

    private PhantomCombat instance = PhantomCombat.getInstance();

    @EventHandler
    public void onDeath(final PlayerDeathEvent e) {
        String defender = e.getEntity().getUniqueId().toString();
        int deaths = Integer.parseInt(instance.data.getOrDefault("stats." + defender + ".deaths", "0"));
        deaths++;
        instance.data.set("stats." + defender + ".deaths", String.valueOf(deaths));
        instance.data.set("stats." + defender + ".killstreak", "0");

        if (e.getEntity().getKiller() != null) {
            String attacker = e.getEntity().getKiller().getUniqueId().toString();
            int kills = Integer.parseInt(instance.data.getOrDefault("stats." + attacker + ".kills", "0"));
            kills++;
            instance.data.set("stats." + attacker + ".kills", String.valueOf(kills));

            int killstreak = Integer.parseInt(instance.data.getOrDefault("stats." + attacker + ".killstreak", "0"));
            killstreak++;
            instance.data.set("stats." + attacker + ".killstreak", String.valueOf(killstreak));

            int highestKillstreak = Integer.parseInt(instance.data.getOrDefault("stats." + attacker + ".highestKillstreak", "0"));
            if (killstreak > highestKillstreak) {
                instance.data.set("stats." + attacker + ".highestKillstreak", String.valueOf(killstreak));
            }
        }
    }
}
