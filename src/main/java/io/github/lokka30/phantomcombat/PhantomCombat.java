package io.github.lokka30.phantomcombat;

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.internal.FlatFile;
import io.github.lokka30.phantomcombat.commands.*;
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
import java.util.logging.Logger;

public class PhantomCombat extends JavaPlugin {

    private static PhantomCombat instance;
    public FlatFile settings;
    public FlatFile messages;
    public FlatFile data;
    boolean configEnabled = false;

    final int settingsCurrentVer = 4;
    final int messagesCurrentVer = 2;
    final int dataCurrentVer = 1;

    public static PhantomCombat getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        log(LogLevel.INFO, "&8&m+-----------------------------+");

        log(LogLevel.INFO, "&8[&71/5&8] &7Checking compatibility...");
        checkCompatibility();

        log(LogLevel.INFO, "&8[&72/5&8] &7Loading files...");
        loadFiles();

        log(LogLevel.INFO, "&8[&73/5&8] &7Registering events...");
        registerEvents();

        log(LogLevel.INFO, "&8[&74/5&8] &7Registering commands...");
        registerCommands();

        log(LogLevel.INFO, "&8[&75/5&8] &7Setting up bStats metrics...");
        new Metrics(this);

        log(LogLevel.INFO, "&8[&7Loaded&8]&7 Thank you for choosing PhantomCombat!");
        log(LogLevel.INFO, "&8&m+-----------------------------+");

        checkUpdates();
    }

    @Override
    public void onDisable() {
        instance = null;
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
            log(LogLevel.WARNING, "You are running an unsupported server version: '&a" + currentVersion + "&7'. Please switch to &a" + suggestedVersion + "&7.");
        }
    }

    /*
    Sets up the files from LightningStorage.
     */
    public void loadFiles() {
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
            log(LogLevel.WARNING, "Unable to retrieve settings file-version, default file installed.");
            saveResource("settings.yml", true);
        } else {
            if (settings.getInt("file-version") != settingsCurrentVer) {
                log(LogLevel.WARNING, "Your settings file is outdated!");
            }
        }

        if (messages.get("file-version") == null) {
            log(LogLevel.WARNING, "Unable to retrieve messages file-version, default file installed.");
            saveResource("messages.yml", true);
        } else {
            if (messages.getInt("file-version") != messagesCurrentVer) {
                log(LogLevel.WARNING, "Your messages file is outdated!");
            }
        }

        if (data.get("file-version") == null) {
            log(LogLevel.WARNING, "Unable to retrieve data file-version, default file installed.");
            saveResource("data.json", true);
        } else {
            if (data.getInt("file-version") != dataCurrentVer) {
                log(LogLevel.WARNING, "Your data file is outdated!");
            }
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
        pm.registerEvents(new LGracePeriod(), this);
        pm.registerEvents(new LPvPSettings(), this);
        pm.registerEvents(new LPvPToggle(), this);
    }

    /*
    Registers the commands.
     */
    public void registerCommands() {
        Objects.requireNonNull(getCommand("stats")).setExecutor(new CStats());
        Objects.requireNonNull(getCommand("deathcoords")).setExecutor(new CDeathCoords());
        Objects.requireNonNull(getCommand("phantomcombat")).setExecutor(new CPhantomCombat());
        Objects.requireNonNull(getCommand("graceperiod")).setExecutor(new CGracePeriod());
        Objects.requireNonNull(getCommand("pvptoggle")).setExecutor(new CPvPToggle());
    }

    /*
    Checks for updates, if enabled in settings.
     */
    public void checkUpdates() {
        if (settings.getBoolean("updater")) {
            log(LogLevel.INFO, "&8[&7Updater&8] &7Checking for updates...");
            new UpdateChecker(this, 12345).getVersion(version -> {
                if (this.getDescription().getVersion().equalsIgnoreCase(version)) {
                    log(LogLevel.INFO, "&8[&7Updater&8] &7You have the latest version installed.");
                } else {
                    log(LogLevel.INFO, "&8[&7Updater&8] &aA new update is available for download!");
                }
            });
        }
    }

    public String colorize(final String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void log(final LogLevel level, final String msg) {
        final Logger logger = getLogger();
        switch (level) {
            case INFO:
                logger.info(colorize("&7" + msg));
                break;
            case WARNING:
                logger.warning(colorize("&7" + msg));
                break;
            case SEVERE:
                logger.severe(colorize("&7" + msg));
                break;
            default:
                throw new IllegalStateException("Unexpected LogLevel: " + level);
        }
    }

    public void actionBar(final Player p, final String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(colorize(msg)));
    }

}
