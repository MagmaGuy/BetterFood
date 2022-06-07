package com.magmaguy.betterfood;

import com.magmaguy.betterfood.commands.BetterFoodCommand;
import com.magmaguy.betterfood.listeners.FoodLevelChangedListener;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by MagmaGuy on 08/12/2017.
 */
public class BetterFood extends JavaPlugin {

    //Should probably have these keys in some sort of utility class
    @Getter
    private final NamespacedKey keyDisabled = new NamespacedKey(this, "enabled");
    @Getter
    private final NamespacedKey keyHideMessage = new NamespacedKey(this, "showmessage");
    @Getter
    private final NamespacedKey keyMode = new NamespacedKey(this, "mode");

    @Override
    public void onEnable() {
        new Metrics(this, 4557);
        this.getServer().getPluginManager().registerEvents(new FoodLevelChangedListener(this), this);
        PluginCommand betterFoodCommand = this.getCommand("betterfood");
        if (betterFoodCommand != null) {
            betterFoodCommand.setExecutor(new BetterFoodCommand(this));
        }
    }

}
