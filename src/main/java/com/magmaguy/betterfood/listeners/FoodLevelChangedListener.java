package com.magmaguy.betterfood.listeners;

import com.magmaguy.betterfood.BetterFood;
import com.magmaguy.betterfood.Food;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class FoodLevelChangedListener implements Listener {
    private static final int MAX_HUNGER = 20;

    private final NamespacedKey keyBetterFoodDisabled;
    private final NamespacedKey keyBetterFoodHideMessage;

    public FoodLevelChangedListener(BetterFood plugin) {
        this.keyBetterFoodDisabled = plugin.getKeyDisabled();
        this.keyBetterFoodHideMessage = plugin.getKeyHideMessage();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChanged(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!player.hasPermission("betterfood.user")) {
            return;
        }

        int playerFoodLevel = player.getFoodLevel();
        int newFoodLevel = event.getFoodLevel();
        if (newFoodLevel >= playerFoodLevel) {
            return;
        }

        PersistentDataContainer playerData = player.getPersistentDataContainer();
        if (playerData.has(keyBetterFoodDisabled, PersistentDataType.BYTE)) {
            return;
        }

        ItemStack validFoodItem = findBestAvailableFood(player, newFoodLevel);
        if (validFoodItem == null) {
            return;
        }
        Food idealFood = Food.get(validFoodItem.getType());

        int newHungerLevel = Math.min(Math.max(newFoodLevel + idealFood.hungerRestored(), 0), MAX_HUNGER);
        float newSaturationLevel = Math.max(player.getSaturation() + idealFood.saturationRestored(), 0);
        newSaturationLevel = Math.min(newSaturationLevel, newHungerLevel);

        if (!playerData.has(keyBetterFoodHideMessage, PersistentDataType.BYTE)) {
            player.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText("You ate some " + prettify(validFoodItem.getType())));
        }

        consumeFood(player, validFoodItem);
        event.setFoodLevel(newHungerLevel);
        player.setSaturation(newSaturationLevel);
    }

    private ItemStack findBestAvailableFood(Player player, int newFoodLevel) {
        Food preferredFood = Food.findBestFood(newFoodLevel);
        if (preferredFood != null) {
            ItemStack matchingFood = findSpecificFood(player, preferredFood.foodMaterial());
            if (matchingFood != null) {
                return matchingFood;
            }
        }

        Food bestAvailableFood = null;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || !isPlainFood(item)) {
                continue;
            }
            Food food = Food.get(item.getType());
            if (food == null) {
                continue;
            }
            bestAvailableFood = chooseBetterFood(bestAvailableFood, food, newFoodLevel);
        }

        if (bestAvailableFood == null) {
            return null;
        }
        return findSpecificFood(player, bestAvailableFood.foodMaterial());
    }

    private Food chooseBetterFood(Food currentBest, Food challenger, int newFoodLevel) {
        if (currentBest == null) {
            return challenger;
        }

        int missingHunger = MAX_HUNGER - newFoodLevel;
        boolean currentFits = currentBest.hungerRestored() <= missingHunger;
        boolean challengerFits = challenger.hungerRestored() <= missingHunger;

        if (currentFits != challengerFits) {
            return challengerFits ? challenger : currentBest;
        }

        if (challengerFits) {
            if (challenger.hungerRestored() != currentBest.hungerRestored()) {
                return challenger.hungerRestored() > currentBest.hungerRestored() ? challenger : currentBest;
            }
            return challenger.saturationRestored() > currentBest.saturationRestored() ? challenger : currentBest;
        }

        int currentOverflow = currentBest.hungerRestored() - missingHunger;
        int challengerOverflow = challenger.hungerRestored() - missingHunger;
        if (challengerOverflow != currentOverflow) {
            return challengerOverflow < currentOverflow ? challenger : currentBest;
        }
        return challenger.saturationRestored() > currentBest.saturationRestored() ? challenger : currentBest;
    }

    private ItemStack findSpecificFood(Player player, Material material) {
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() != material) {
                continue;
            }
            if (!isPlainFood(item)) {
                continue;
            }
            return item;
        }
        return null;
    }

    private void consumeFood(Player player, ItemStack validFoodItem) {
        Material remainderMaterial = getRemainderMaterial(validFoodItem.getType());
        validFoodItem.setAmount(validFoodItem.getAmount() - 1);
        if (remainderMaterial == null) {
            return;
        }

        ItemStack remainder = new ItemStack(remainderMaterial);
        var leftovers = player.getInventory().addItem(remainder);
        leftovers.values().forEach(itemStack ->
                player.getWorld().dropItemNaturally(player.getLocation(), itemStack));
    }

    private @Nullable Material getRemainderMaterial(Material material) {
        try {
            return material.getCraftingRemainingItem();
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean isPlainFood(ItemStack itemStack) {
        Material material = itemStack.getType();
        if (material.isAir() || Food.isExcluded(material)) {
            return false;
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            return true;
        }
        if (itemMeta.hasDisplayName()
                || itemMeta.hasItemName()
                || itemMeta.hasLore()
                || itemMeta.hasCustomModelData()
                || itemMeta.hasEnchants()
                || itemMeta.hasAttributeModifiers()
                || itemMeta.isUnbreakable()) {
            return false;
        }
        return itemMeta.getPersistentDataContainer().isEmpty();
    }

    private String prettify(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder stringBuilder = new StringBuilder();
        for (String word : words) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(' ');
            }
            stringBuilder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return stringBuilder.toString();
    }
}
