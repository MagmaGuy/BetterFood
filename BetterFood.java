package com.magmaguy.betterfood;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bstats.Metrics;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by MagmaGuy on 08/12/2017.
 */
public class BetterFood extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Metrics metrics = new Metrics(this);
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    List<Material> validFoodMaterialSat1 = new ArrayList<Material>(Arrays.asList(
            Material.DRIED_KELP
    ));

    List<Material> validFoodMaterialSat2 = new ArrayList<Material>(Arrays.asList(
            Material.COOKIE, Material.MELON_SLICE, Material.SWEET_BERRIES
    ));

    List<Material> validFoodMaterialSat4 = new ArrayList<Material>(Arrays.asList(
            Material.APPLE
    ));

    List<Material> validFoodMaterialSat5 = new ArrayList<Material>(Arrays.asList( //fishes have different data values from cooked_fish
            Material.BAKED_POTATO, Material.BREAD, Material.COOKED_COD, Material.COOKED_RABBIT
    ));

    List<Material> validFoodMaterialSat6 = new ArrayList<Material>(Arrays.asList(
            Material.BEETROOT_SOUP, Material.COOKED_CHICKEN, Material.COOKED_MUTTON, Material.COOKED_MUTTON, Material.COOKED_SALMON, Material.MUSHROOM_STEW
    ));

    List<Material> validFoodMaterialSat8 = new ArrayList<Material>(Arrays.asList(
            Material.COOKED_PORKCHOP, Material.PUMPKIN_PIE, Material.COOKED_BEEF
    ));

    List<Material> validFoodMaterialSat10 = new ArrayList<Material>(Arrays.asList(
            Material.RABBIT_STEW
    ));

    @EventHandler(priority = EventPriority.LOWEST)
    public void autoFeed(FoodLevelChangeEvent event) {

        if (event.getEntity() instanceof Player) {

            Player player = (Player) event.getEntity();

            if (!player.hasPermission("betterfood.user"))
                return;

            Inventory playerInventory = player.getInventory();

            if (event.getFoodLevel() <= 20 - 4)
                if (foodLevelIterator(player, playerInventory, event.getFoodLevel(), 4, validFoodMaterialSat1)) {
                    event.setCancelled(true);
                    return;
                }

            if (event.getFoodLevel() <= 20 - 4)
                if (foodLevelIterator(player, playerInventory, event.getFoodLevel(), 4, validFoodMaterialSat2)) {
                    event.setCancelled(true);
                    return;
                }

            if (event.getFoodLevel() <= 20 - 4)
                if (foodLevelIterator(player, playerInventory, event.getFoodLevel(), 4, validFoodMaterialSat4)) {
                    event.setCancelled(true);
                    return;
                }

            if (event.getFoodLevel() <= 20 - 5)
                if (foodLevelIterator(player, playerInventory, event.getFoodLevel(), 5, validFoodMaterialSat5)) {
                    event.setCancelled(true);
                    return;
                }

            if (event.getFoodLevel() <= 20 - 6)
                if (foodLevelIterator(player, playerInventory, event.getFoodLevel(), 6, validFoodMaterialSat6)) {
                    event.setCancelled(true);
                    return;
                }

            if (event.getFoodLevel() <= 20 - 8)
                if (foodLevelIterator(player, playerInventory, event.getFoodLevel(), 8, validFoodMaterialSat8)) {
                    event.setCancelled(true);
                    return;
                }

            if (event.getFoodLevel() <= 20 - 10)
                if (foodLevelIterator(player, playerInventory, event.getFoodLevel(), 10, validFoodMaterialSat10)) {
                    event.setCancelled(true);
                    return;
                }

        }

    }

    private void playerMessage(Player player, String name){
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("[BetterFood] You ate a " + name));
    }

    private boolean foodLevelIterator(Player player, Inventory playerInventory, int playerFoodLevel, int foodLevelThreshold,
                                      List<Material> materialList) {

        for (Material material : materialList)
            if (playerInventory.contains(material))
                for (ItemStack itemStack : playerInventory)
                    if (itemStack != null && itemStack.getType().equals(material))
                        if (!itemStack.getItemMeta().hasLore() && !itemStack.getItemMeta().hasDisplayName()) {
                            itemStack.setAmount(itemStack.getAmount() - 1);
                            player.setFoodLevel(playerFoodLevel + foodLevelThreshold);
                            playerMessage(player, WordUtils.capitalizeFully(itemStack.getType().name().toLowerCase().replace("_", " ")));
                            return true;
                        }

        return false;

    }

}
