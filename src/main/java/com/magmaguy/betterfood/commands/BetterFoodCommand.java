package com.magmaguy.betterfood.commands;

import com.magmaguy.betterfood.BetterFood;
import com.magmaguy.magmacore.nightbreak.NightbreakCatalogMenu;
import com.magmaguy.magmacore.nightbreak.NightbreakPluginHotSwap;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BetterFoodCommand implements TabExecutor {
    private final BetterFood plugin;

    public BetterFoodCommand(BetterFood betterFoodPlugin) {
        this.plugin = betterFoodPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            Player p = requireUserPlayer(sender, "[BF] Use /betterfood downloadpluginupdate to check for plugin updates.");
            if (p != null) toggleBetterFoodEnabled(p);
            return true;
        }

        if (args.length == 1) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "downloadall", "downloadpluginupdate" -> downloadPluginUpdate(sender);
                case "applypluginupdate" -> applyPluginUpdate(sender);
                case "downloadallcontent" -> noContentPacks(sender);
                case "setup" -> openSetup(sender);
                case "recommendedplugins" -> recommendedPlugins(sender);
                case "toggleeating" -> {
                    Player p = requireUserPlayer(sender, "[BF] BetterFood settings can only be changed in-game.");
                    if (p != null) toggleBetterFoodEnabled(p);
                }
                case "togglemessage" -> {
                    Player p = requireUserPlayer(sender, "[BF] BetterFood settings can only be changed in-game.");
                    if (p != null) toggleBetterFoodMessage(p);
                }
                default -> sendUsage(sender);
            }
            return true;
        }

        sendUsage(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("betterfood.user")) {
                completions.add("toggleEating");
                completions.add("toggleMessage");
            }
            if (sender.hasPermission("betterfood.admin")) {
                completions.add("downloadpluginupdate");
                completions.add("applypluginupdate");
                completions.add("downloadallcontent");
                completions.add("setup");
                completions.add("recommendedplugins");
            }

            String prefix = args[0].toLowerCase(Locale.ROOT);
            return completions.stream()
                    .filter(completion -> completion.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .toList();
        }

        return List.of();
    }

    private void sendUsage(CommandSender sender) {
        if (sender.hasPermission("betterfood.admin")) {
            sender.sendMessage("[BF] Invalid command. Use /betterfood setup, /betterfood recommendedplugins, /betterfood toggleEating, /betterfood toggleMessage, /betterfood downloadpluginupdate, or /betterfood applypluginupdate.");
            return;
        }
        sender.sendMessage("[BF] Invalid command. Use /betterfood toggleEating or /betterfood toggleMessage.");
    }

    private Player requireUserPlayer(CommandSender sender, String consoleMessage) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(consoleMessage);
            return null;
        }
        if (!sender.hasPermission("betterfood.user")) {
            sender.sendMessage("[BF] You do not have permission to change BetterFood settings.");
            return null;
        }
        return player;
    }

    private void toggleBetterFoodEnabled(Player player) {
        toggleFlag(player, plugin.getKeyDisabled(), "[BF] Automatic Eating: ON", "[BF] Automatic Eating: OFF");
    }

    private void toggleBetterFoodMessage(Player player) {
        toggleFlag(player, plugin.getKeyHideMessage(), "[BF] Now showing food consumption notifications", "[BF] No longer showing food consumption notifications");
    }

    //Both flags mark the feature as off when present: removing the flag enables, setting it disables
    private void toggleFlag(Player player, NamespacedKey key, String enabledMessage, String disabledMessage) {
        PersistentDataContainer playerData = player.getPersistentDataContainer();

        if (playerData.has(key, PersistentDataType.BYTE)) {
            playerData.remove(key);
            player.sendMessage(enabledMessage);
        } else {
            playerData.set(key, PersistentDataType.BYTE, (byte)0);
            player.sendMessage(disabledMessage);
        }
    }

    private boolean denyNonAdmin(CommandSender sender, String message) {
        if (sender.hasPermission("betterfood.admin"))
            return false;
        sender.sendMessage(message);
        return true;
    }

    private void downloadPluginUpdate(CommandSender sender) {
        if (denyNonAdmin(sender, "[BF] You do not have permission to update the BetterFood plugin."))
            return;
        NightbreakPluginUpdater.downloadPluginUpdateAsync(plugin, BetterFood.NIGHTBREAK_PLUGIN_SPEC, sender, null);
    }

    private void applyPluginUpdate(CommandSender sender) {
        if (denyNonAdmin(sender, "[BF] You do not have permission to update the BetterFood plugin."))
            return;
        NightbreakPluginHotSwap.applyStagedUpdate(plugin, sender);
    }

    private void noContentPacks(CommandSender sender) {
        if (denyNonAdmin(sender, "[BF] You do not have permission to manage BetterFood content."))
            return;
        sender.sendMessage("[BF] BetterFood does not have content packs. Use /betterfood setup for plugin updates and settings.");
    }

    private void openSetup(CommandSender sender) {
        if (denyNonAdmin(sender, "[BF] You do not have permission to open BetterFood setup."))
            return;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("[BF] BetterFood setup can only be opened in-game.");
            return;
        }
        NightbreakSetupControls.openPluginSetupShell(plugin, player, BetterFood.NIGHTBREAK_PLUGIN_SPEC);
    }

    private void recommendedPlugins(CommandSender sender) {
        if (denyNonAdmin(sender, "[BF] You do not have permission to view BetterFood recommendations."))
            return;
        if (sender instanceof Player player) {
            NightbreakCatalogMenu.openRecommendations(plugin, player, BetterFood.NIGHTBREAK_PLUGIN_SPEC);
            return;
        }
        NightbreakCatalogMenu.sendRecommendations(sender, BetterFood.NIGHTBREAK_PLUGIN_SPEC);
    }
}
