package com.magmaguy.betterfood;

import com.magmaguy.betterfood.commands.BetterFoodCommand;
import com.magmaguy.betterfood.listeners.FoodLevelChangedListener;
import com.magmaguy.magmacore.MagmaCore;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginSpec;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginUpdater;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by MagmaGuy on 08/12/2017.
 */
public class BetterFood extends JavaPlugin {
    public static final NightbreakPluginSpec NIGHTBREAK_PLUGIN_SPEC = new NightbreakPluginSpec(
            "BetterFood",
            "betterfood",
            "betterfood.admin",
            "betterfood.admin",
            "betterfood.admin",
            "https://nightbreak.io/plugin/betterfood/",
            "Reloaded BetterFood.", // Inert: only NightbreakPluginBootstrap sends this, and BetterFood does not use the bootstrap.
            false,
            false,
            false);

    // PDC key literals "enabled"/"showmessage" are legacy and deliberately inverted relative to their meaning (presence = disabled / hide). Never change them: they are persisted on player data.
    @Getter
    private final NamespacedKey keyDisabled = new NamespacedKey(this, "enabled");
    @Getter
    private final NamespacedKey keyHideMessage = new NamespacedKey(this, "showmessage");

    @Override
    public void onLoad() {
        MagmaCore.createInstance(this);
    }

    @Override
    public void onEnable() {
        MagmaCore.onEnable(this);
        new Metrics(this, 4557);
        Food.logCoverageAudit(getLogger());
        this.getServer().getPluginManager().registerEvents(new FoodLevelChangedListener(this), this);
        PluginCommand betterFoodCommand = this.getCommand("betterfood");
        if (betterFoodCommand != null) {
            BetterFoodCommand command = new BetterFoodCommand(this);
            betterFoodCommand.setExecutor(command);
            betterFoodCommand.setTabCompleter(command);
        }
        // Blank first arg means "no Spigot resource id" - this is a Nightbreak-only version check.
        MagmaCore.checkVersionUpdate("", NIGHTBREAK_PLUGIN_SPEC.contentUrl());
        NightbreakPluginUpdater.autoDownloadPluginUpdateIfEnabled(this, NIGHTBREAK_PLUGIN_SPEC);
    }

    @Override
    public void onDisable() {
        MagmaCore.shutdown(this);
    }

}
