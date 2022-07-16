package com.magmaguy.betterfood.commands;

import com.magmaguy.betterfood.BetterFood;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BetterFoodCommand implements TabExecutor {
    private final BetterFood plugin;

    public BetterFoodCommand(BetterFood betterFoodPlugin) {
        this.plugin = betterFoodPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (sender instanceof Player player) {

            if (args.length == 0) {
                toggleBetterFoodEnabled(player);
            }

            if (args.length == 1 ) {
                switch (args[0]) {
                    case "toggleEating" -> toggleBetterFoodEnabled(player);
                    case "toggleMessage" -> toggleBetterFoodMessage(player);
                    default -> player.sendMessage("[EM] Invalid command");
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if (args.length == 1) {
            return List.of(
                    "toggleEating",
                    "toggleMessage"
            );
        }

        return null;
    }

    private void toggleBetterFoodEnabled(Player player) {

        NamespacedKey betterFoodEnabledKey = plugin.getKeyDisabled();
        PersistentDataContainer playerData = player.getPersistentDataContainer();

        if (playerData.has(betterFoodEnabledKey, PersistentDataType.BYTE)) {
            playerData.remove(betterFoodEnabledKey);
            player.sendMessage("[BF] Automatic Eating: ON");
        } else {
            playerData.set(betterFoodEnabledKey, PersistentDataType.BYTE, (byte)0);
            player.sendMessage("[BF] Automatic Eating: OFF");
        }

    }

    private void toggleBetterFoodMessage(Player player) {

        NamespacedKey betterFoodShowMessageKey = plugin.getKeyHideMessage();
        PersistentDataContainer playerData = player.getPersistentDataContainer();

        if (playerData.has(betterFoodShowMessageKey, PersistentDataType.BYTE)) {
            playerData.remove(betterFoodShowMessageKey);
            player.sendMessage("[BF] Now showing food consumption notifications");
        } else {
            playerData.set(betterFoodShowMessageKey, PersistentDataType.BYTE, (byte)0);
            player.sendMessage("[BF] No longer showing food consumption notifications");
        }
    }
}
