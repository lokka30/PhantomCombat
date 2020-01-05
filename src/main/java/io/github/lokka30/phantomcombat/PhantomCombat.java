package io.github.lokka30.phantomcombat;

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomcombat.commands.CDeathCoords;
import io.github.lokka30.phantomcombat.commands.CPhantomCombat;
import io.github.lokka30.phantomcombat.commands.CStats;
import io.github.lokka30.phantomcombat.listeners.*;
import io.github.lokka30.phantomcombat.utils.UpdateChecker;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class PhantomCombat extends JavaPlugin {

    private static PhantomCombat instance;
    public FlatFile settings;
    public FlatFile messages;
    public FlatFile data;
    boolean configEnabled = false;

    public static PhantomCombat getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        log(LogLevel.STATUS, "&8[&7Loading&8] PhantomCombat&7 developed by lokka30");

        log(LogLevel.STATUS, "&8[&71/5&8] &7Checking compatibility...");
        checkCompatibility();

        log(LogLevel.STATUS, "&8[&72/5&8] &7Managing configurations...");
        manageConfigs();

        log(LogLevel.STATUS, "&8[&73/5&8] &7Registering events...");
        registerEvents();

        log(LogLevel.STATUS, "&8[&74/5&8] &7Registering commands...");
        registerCommands();

        log(LogLevel.STATUS, "&8[&75/5&8] &7Setting up bStats metrics...");
        new Metrics(this);

        log(LogLevel.STATUS, "&8[&7Loaded&8]&7 Thank you for chosing PhantomCombat!");

        checkUpdates();
    }

    @Override
    public void onDisable() {
        log(LogLevel.STATUS, "&8[&7Disabling&8] PhantomCombat&7 developed by lokka30");

        log(LogLevel.STATUS, "&8[&71/1&8] &7Unregistering instance...");
        instance = null;

        log(LogLevel.STATUS, "&8[&7Disabled&8]&7 Goodbye :(");
    }

    /*
    This method checks if the server is running the correct version,
    using the api-version indicated in plugin.yml.
     */
    public void checkCompatibility() {
        final String currentVersion = getServer().getVersion();
        final String suggestedVersion = getDescription().getAPIVersion();

        assert suggestedVersion != null;
        if (!getServer().getVersion().contains(suggestedVersion)) {
            log(LogLevel.WARNING, "&8[&71/5&8] &7You are running an unsupported server version: '&a" + currentVersion + "&7'. Please switch to &a" + suggestedVersion + "&7.");
        }
    }

    /*
    Sets up the files from LightningStorage.
     */
    public void manageConfigs() {
        settings = LightningBuilder
                .fromFile(new File("plugins/PhantomCombat/settings"))
                .addInputStreamFromResource("settings.yml")
                .createYaml();
        messages = LightningBuilder
                .fromFile(new File("plugins/PhantomCombat/messages"))
                .addInputStreamFromResource("messages.yml")
                .createYaml();
        data = LightningBuilder
                .fromFile(new File("plugins/PhantomCombat/data"))
                .addInputStreamFromResource("data.json")
                .createJson();

        if (settings.get("file-version") == null) {
            saveResource("settings.yml", true);
        }
        if (messages.get("file-version") == null) {
            saveResource("messages.yml", true);
        }
        if (data.get("file-version") == null) {
            saveResource("data.json", true);
            data.set("file-version", 1);
        }

        configEnabled = true;
    }

    /*
    Registers the events.
     */
    public void registerEvents() {
        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new LCombatMode(), this);
        pm.registerEvents(new LStats(), this);
        pm.registerEvents(new LDeathCoords(), this);
        pm.registerEvents(new LBlood(), this);
        pm.registerEvents(new LArmorHitSound(), this);
    }

    /*
    Registers the commands.
     */
    public void registerCommands() {
        Objects.requireNonNull(getCommand("stats")).setExecutor(new CStats());
        Objects.requireNonNull(getCommand("deathcoords")).setExecutor(new CDeathCoords());
        Objects.requireNonNull(getCommand("phantomcombat")).setExecutor(new CPhantomCombat());
    }

    /*
    Checks for updates, if enabled in settings.
     */
    public void checkUpdates() {
        if (settings.getBoolean("updater")) {
            log(LogLevel.STATUS, "&8[&7Updater&8] &7Checking for updates...");
            new UpdateChecker(this, 12345).getVersion(version -> {
                if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    log(LogLevel.STATUS, "&8[&7Updater&8] &7You have the latest version installed.");
                } else {
                    log(LogLevel.INFO, "&8[&7Updater&8] &a&nA new update is available for download.");
                }
            });
        }
    }

    public String colorize(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void log(LogLevel level, String msg) {
        String prefix;
        switch (level) {
            case STATUS:
                prefix = "&7STATUS";
                break;
            case INFO:
                prefix = "&fINFO";
                break;
            case WARNING:
                prefix = "&eWARNING";
                break;
            case SEVERE:
                prefix = "&cSEVERE";
                break;
            default:
                prefix = "&0Invalid Log Level!";
                break;
        }

        getServer().getConsoleSender().sendMessage(colorize("&8[" + prefix + "&8] &a&lPhantomCombat: &7" + msg));
    }

    public void actionBar(Player p, String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(colorize(msg)));
    }

}
