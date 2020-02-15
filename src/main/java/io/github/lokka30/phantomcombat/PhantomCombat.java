package io.github.lokka30.phantomcombat;

import de.leonhard.storage.LightningBuilder;
import de.leonhard.storage.internal.FlatFile;
import de.leonhard.storage.internal.exception.LightningValidationException;
import io.github.lokka30.phantomcombat.commands.*;
import io.github.lokka30.phantomcombat.listeners.*;
import io.github.lokka30.phantomcombat.utils.LogLevel;
import io.github.lokka30.phantomcombat.utils.UpdateChecker;
import io.github.lokka30.phantomcombat.utils.Utils;
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

    final int settingsCurrentVer = 6;
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

        log(LogLevel.INFO, "Loaded successfuly, thank you for choosing PhantomCombat!");

        checkUpdates();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    /*
    This method checks if the server is running the correct version.
     */
    private void checkCompatibility() {
        final String currentVersion = getServer().getVersion();
        final String recommendedVersion = Utils.getRecommendedServerVersion();
        if (currentVersion.contains(recommendedVersion)) {
            log(LogLevel.INFO, "Server is running supported version &a" + currentVersion + "&7.");
        } else {
            log(LogLevel.WARNING, " ");
            log(LogLevel.WARNING, "Server is running &cunsupported&7 version &a" + currentVersion + "&7.");
            log(LogLevel.WARNING, "The recommended version is &a" + recommendedVersion + "&7.");
            log(LogLevel.WARNING, "You will not get support with the plugin whilst using an unsupported version!");
            log(LogLevel.WARNING, " ");
        }
    }

    /*
    Sets up the files from LightningStorage.
     */
    private void loadFiles() {
        //Load the files
        final PluginManager pm = getServer().getPluginManager();
        final String path = "plugins/PhantomCombat/";
        try {
            settings = LightningBuilder
                    .fromFile(new File(path + "settings"))
                    .addInputStreamFromResource("settings.yml")
                    .createYaml();
        } catch (LightningValidationException e) {
            log(LogLevel.SEVERE, "Unable to load &asettings.yml&7!");
            pm.disablePlugin(this);
            return;
        }

        try {
            messages = LightningBuilder
                    .fromFile(new File(path + "messages"))
                    .addInputStreamFromResource("messages.yml")
                    .createYaml();
        } catch (LightningValidationException e) {
            log(LogLevel.SEVERE, "Unable to load &amessages.yml&7!");
            pm.disablePlugin(this);
            return;
        }

        try {
            data = LightningBuilder
                    .fromFile(new File(path + "data"))
                    .addInputStreamFromResource("data.json")
                    .createJson();
        } catch (LightningValidationException e) {
            log(LogLevel.SEVERE, "Unable to load &adata.json&7!");
            pm.disablePlugin(this);
            return;
        }

        //Check if they exist
        final File settingsFile = new File(path + "settings.yml");
        final File messagesFile = new File(path + "messages.yml");
        final File dataFile = new File(path + "data.json");

        if (!(settingsFile.exists() && !settingsFile.isDirectory())) {
            log(LogLevel.INFO, "File &asettings.yml&7 doesn't exist. Creating it now.");
            saveResource("settings.yml", false);
        }

        if (!(messagesFile.exists() && !messagesFile.isDirectory())) {
            log(LogLevel.INFO, "File &amessages.yml&7 doesn't exist. Creating it now.");
            saveResource("messages.yml", false);
        }

        if (!(dataFile.exists() && !dataFile.isDirectory())) {
            log(LogLevel.INFO, "File &adata.json&7 doesn't exist. Creating it now.");
            saveResource("data.json", false);
        }

        //Check their versions -- default set to 0 instead of recommended
        //version as if they are missing the file version then they haven't configured the file properly.
        if (settings.get("file-version", 0) != Utils.getRecommendedSettingsVersion()) {
            log(LogLevel.SEVERE, "File &asettings.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        if (messages.get("file-version", 0) != Utils.getRecommendedMessagesVersion()) {
            log(LogLevel.SEVERE, "File &amessages.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
        }

        if (data.get("file-version", 0) != Utils.getRecommendedDataVersion()) {
            log(LogLevel.SEVERE, "File &adata.yml&7 is out of date! Errors are likely to occur! Reset it or merge the old values to the new file.");
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
            log(LogLevel.INFO, "&8[&7Update Checker&8] &7Starting version comparison...");
            new UpdateChecker(this, 74060).getVersion(version -> {
                final String currentVersion = getDescription().getVersion();
                if (currentVersion.equalsIgnoreCase(version)) {
                    log(LogLevel.INFO, "&8[&7Update Checker&8] &7You're running the latest version.'");
                } else {
                    log(LogLevel.INFO, "&8[&7Update Checker&8] &7There's a new update available: &a" + version + "&7. You're running &a" + currentVersion + "&7.");
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
