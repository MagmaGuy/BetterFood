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
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FoodLevelChangedListener implements Listener {

    private static final int MAX_HUNGER = 20;
    private static final Food[] FOODS_ORDERED_BY_HUNGER_RESTORED = Food.getFood().stream()
            .sorted(Comparator.comparing(Food::getHungerRestored)
                    .thenComparing(Food::getSaturationRestored).reversed())
            .toArray(Food[]::new);

    //Stores where food search should start iterating the FOOD_BY_HUNGER array for each possible hunger level
    private static final Map<Integer, Integer> HUNGER_TO_FOODBYHUNGER_OFFSET = new HashMap<>();

    static {

        //Populates the Hunger -> HUNGER_TO_FOODBYHUNGER_OFFSET map
        int currentHungerStartIndex = 0;
        int nextHungerStartIndex = getNextHungerStartIndex(currentHungerStartIndex);
        int nextHungerRestoreAmount = FOODS_ORDERED_BY_HUNGER_RESTORED[nextHungerStartIndex].getHungerRestored();

        for (int foodLevel = 0; foodLevel < MAX_HUNGER; foodLevel++) {
            if (nextHungerRestoreAmount >= MAX_HUNGER - foodLevel) {
                currentHungerStartIndex = nextHungerStartIndex;
                nextHungerStartIndex = getNextHungerStartIndex(currentHungerStartIndex);
                nextHungerRestoreAmount = FOODS_ORDERED_BY_HUNGER_RESTORED[nextHungerStartIndex].getHungerRestored();
            }
            HUNGER_TO_FOODBYHUNGER_OFFSET.put(foodLevel, currentHungerStartIndex);
        }
    }

    private final NamespacedKey keyBetterFoodDisabled;
    private final NamespacedKey keyBetterFoodHideMessage;
    public FoodLevelChangedListener(BetterFood plugin) {
        this.keyBetterFoodDisabled = plugin.getKeyDisabled();
        this.keyBetterFoodHideMessage = plugin.getKeyHideMessage();
    }

    @EventHandler
    public void onFoodLevelChanged(FoodLevelChangeEvent event) {

        Player player = (Player) event.getEntity();
        if (!player.hasPermission("betterfood.user"))
            return;

        //Only acts on events where hunger is being lowered
        int playerFoodLevel = player.getFoodLevel();
        int newFoodLevel = event.getFoodLevel();
        if (newFoodLevel >= playerFoodLevel)
            return;

        PersistentDataContainer playerData = player.getPersistentDataContainer();
        if (playerData.has(keyBetterFoodDisabled, PersistentDataType.BYTE))
            return;

        PlayerInventory playerInventory = player.getInventory();

        //Iterates the Player inventory once creating a map of ItemStacks as follows:
        //Key: ItemStack Material
        //Value: Set of all ItemStacks in the inventory that match the Material
        //Used to restrict further inventory iterations to only the ItemStacks relevant to the searched Food
        Map<@NotNull Material, @NotNull Set<ItemStack>> inventoryMap = new HashMap<>();
        for (ItemStack item : playerInventory) {
            if (item == null) continue;
            Material itemMaterial = item.getType();
            inventoryMap.putIfAbsent(itemMaterial, new HashSet<>());
            inventoryMap.get(itemMaterial).add(item);
        }

        if (HUNGER_TO_FOODBYHUNGER_OFFSET.get(newFoodLevel) == null) return;

        //Determines which index of FOOD_BY_HUNGER_RESTORED to start iterating from based on hunger level
        int indexOffset = HUNGER_TO_FOODBYHUNGER_OFFSET.get(newFoodLevel);

        //The logic below searches for the first 'valid' food item in the Player inventory
        //Will iterate starting from the position of the first food in the array that would perfectly fill the hunger bar
        //Will continue iterating along the array in the direction of foods that are less nourishing until Food is found
        ItemStack validFoodItem = null;
        for (int i = indexOffset; i < FOODS_ORDERED_BY_HUNGER_RESTORED.length - 1; i++) {

            Food food = FOODS_ORDERED_BY_HUNGER_RESTORED[i];
            Material foodMaterial = food.getFoodMaterial();

            if (!inventoryMap.containsKey(foodMaterial))
                continue;

            Set<ItemStack> inventoryFoodItems = inventoryMap.get(foodMaterial);
            for (ItemStack inventoryFoodItem : inventoryFoodItems) {

                //Ensures the target ItemStack does not contain a customized item
                ItemMeta playerFoodMeta = inventoryFoodItem.getItemMeta();
                if (playerFoodMeta == null
                        || playerFoodMeta.hasDisplayName()
                        || playerFoodMeta.hasLore())
                    continue;

                validFoodItem = inventoryFoodItem;
                break;
            }

            if (validFoodItem == null)
                continue;

            //Restricts max and min hunger/saturation values following MC logic
            int newHungerLevel = Math.min(Math.max(newFoodLevel + food.getHungerRestored(), 0), 20);
            float newSaturationLevel = Math.max(player.getSaturation() + food.getSaturationRestored(), 0);
            newSaturationLevel = Math.min(newSaturationLevel, newHungerLevel);

            if (!playerData.has(keyBetterFoodHideMessage, PersistentDataType.BYTE))
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText("You ate some " + validFoodItem.getType()));

            //Ensure all actions taken involving the ItemStack happen prior to decrement
            //Otherwise could see issues related to ItemStack depletion
            validFoodItem.setAmount(validFoodItem.getAmount() - 1);
            event.setFoodLevel(newHungerLevel);
            player.setSaturation(newSaturationLevel);

            break;
        }
    }

    //Gets the index of the next Food in the target array with a different Hunger Restoration value
    private static int getNextHungerStartIndex(int currentIndex) {

        if (currentIndex > FOODS_ORDERED_BY_HUNGER_RESTORED.length)
            return -1;

        int currentHungerRestored = FOODS_ORDERED_BY_HUNGER_RESTORED[currentIndex].getHungerRestored();

        for (int i = currentIndex; i < FOODS_ORDERED_BY_HUNGER_RESTORED.length - 1; i++) {

            int nextHungerRestoreAmount = FOODS_ORDERED_BY_HUNGER_RESTORED[i].getHungerRestored();
            if (nextHungerRestoreAmount == currentHungerRestored)
                continue;

            return i;
        }

        return currentIndex;
    }
}
