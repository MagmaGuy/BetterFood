package com.magmaguy.betterfood.commands;

import com.magmaguy.betterfood.BetterFood;
import com.magmaguy.magmacore.nightbreak.NightbreakCatalogMenu;
import com.magmaguy.magmacore.nightbreak.NightbreakSetupControls;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginUpdater;
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
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("[BF] Use /betterfood downloadpluginupdate to check for plugin updates.");
                return true;
            }
            if (!sender.hasPermission("betterfood.user")) {
                sender.sendMessage("[BF] You do not have permission to change BetterFood settings.");
                return true;
            }
            toggleBetterFoodEnabled(player);
            return true;
        }

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "downloadall", "downloadpluginupdate" -> downloadPluginUpdate(sender);
                case "downloadallcontent" -> noContentPacks(sender);
                case "setup" -> openSetup(sender);
                case "recommendedplugins" -> recommendedPlugins(sender);
                case "toggleeating" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("[BF] BetterFood settings can only be changed in-game.");
                        return true;
                    }
                    if (!sender.hasPermission("betterfood.user")) {
                        sender.sendMessage("[BF] You do not have permission to change BetterFood settings.");
                        return true;
                    }
                    toggleBetterFoodEnabled(player);
                }
                case "togglemessage" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("[BF] BetterFood settings can only be changed in-game.");
                        return true;
                    }
                    if (!sender.hasPermission("betterfood.user")) {
                        sender.sendMessage("[BF] You do not have permission to change BetterFood settings.");
                        return true;
                    }
                    toggleBetterFoodMessage(player);
                }
                default -> sender.sendMessage("[BF] Invalid command. Use /betterfood setup, /betterfood recommendedplugins, /betterfood toggleEating, /betterfood toggleMessage, or /betterfood downloadpluginupdate.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if (args.length == 1) {
            return List.of(
                    "toggleEating",
                    "toggleMessage",
                    "downloadpluginupdate",
                    "downloadall",
                    "downloadallcontent",
                    "setup",
                    "recommendedplugins"
            );
        }

        return List.of();
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

    private void downloadPluginUpdate(CommandSender sender) {
        if (!sender.hasPermission("betterfood.admin")) {
            sender.sendMessage("[BF] You do not have permission to update the BetterFood plugin.");
            return;
        }
        NightbreakPluginUpdater.downloadPluginUpdateAsync(plugin, BetterFood.NIGHTBREAK_PLUGIN_SPEC, sender, null);
    }

    private void noContentPacks(CommandSender sender) {
        if (!sender.hasPermission("betterfood.admin")) {
            sender.sendMessage("[BF] You do not have permission to update BetterFood.");
            return;
        }
        sender.sendMessage("[BF] BetterFood does not have content packs. Use /betterfood setup for plugin updates and settings.");
    }

    private void openSetup(CommandSender sender) {
        if (!sender.hasPermission("betterfood.admin")) {
            sender.sendMessage("[BF] You do not have permission to open BetterFood setup.");
            return;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("[BF] BetterFood setup can only be opened in-game.");
            return;
        }
        NightbreakSetupControls.openPluginSetupShell(plugin, player, BetterFood.NIGHTBREAK_PLUGIN_SPEC);
    }

    private void recommendedPlugins(CommandSender sender) {
        if (!sender.hasPermission("betterfood.admin")) {
            sender.sendMessage("[BF] You do not have permission to view BetterFood recommendations.");
            return;
        }
        if (sender instanceof Player player) {
            NightbreakCatalogMenu.openRecommendations(plugin, player, BetterFood.NIGHTBREAK_PLUGIN_SPEC);
            return;
        }
        NightbreakCatalogMenu.sendRecommendations(sender, BetterFood.NIGHTBREAK_PLUGIN_SPEC);
    }
}
