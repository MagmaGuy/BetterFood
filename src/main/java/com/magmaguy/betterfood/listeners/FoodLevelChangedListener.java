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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodLevelChangedListener implements Listener {

    private static final int MAX_HUNGER = 20;
    private static final Food[] FOODS_ORDERED_BY_HUNGER_RESTORED = Food.getFood().stream()
            .sorted(Comparator.comparing(Food::getHungerRestored)
                    .thenComparing(Food::getSaturationRestored)
                    .reversed()
                    .thenComparing(food -> food.getFoodMaterial().name()))
            .toArray(Food[]::new);

    //Stores where food search should start iterating the FOODS_ORDERED_BY_HUNGER_RESTORED array for each possible hunger level
    private static final int[] HUNGER_TO_FOOD_INDEX_OFFSET = new int[MAX_HUNGER];

    //Maps each food Material to its index in FOODS_ORDERED_BY_HUNGER_RESTORED
    private static final Map<Material, Integer> MATERIAL_TO_FOOD_INDEX = new EnumMap<>(Material.class);

    static {

        //Populates the food Material -> FOODS_ORDERED_BY_HUNGER_RESTORED index map
        for (int i = 0; i < FOODS_ORDERED_BY_HUNGER_RESTORED.length; i++)
            MATERIAL_TO_FOOD_INDEX.put(FOODS_ORDERED_BY_HUNGER_RESTORED[i].getFoodMaterial(), i);

        //Populates the hunger level -> FOODS_ORDERED_BY_HUNGER_RESTORED start-index array
        int currentHungerStartIndex = 0;
        int nextHungerStartIndex = getNextHungerStartIndex(currentHungerStartIndex);
        int nextHungerRestoreAmount = FOODS_ORDERED_BY_HUNGER_RESTORED[nextHungerStartIndex].getHungerRestored();

        for (int foodLevel = 0; foodLevel < MAX_HUNGER; foodLevel++) {
            if (nextHungerRestoreAmount >= MAX_HUNGER - foodLevel) {
                currentHungerStartIndex = nextHungerStartIndex;
                nextHungerStartIndex = getNextHungerStartIndex(currentHungerStartIndex);
                nextHungerRestoreAmount = FOODS_ORDERED_BY_HUNGER_RESTORED[nextHungerStartIndex].getHungerRestored();
            }
            HUNGER_TO_FOOD_INDEX_OFFSET[foodLevel] = currentHungerStartIndex;
        }
    }

    private final NamespacedKey keyBetterFoodDisabled;
    private final NamespacedKey keyBetterFoodHideMessage;
    public FoodLevelChangedListener(BetterFood plugin) {
        this(plugin.getKeyDisabled(), plugin.getKeyHideMessage());
    }

    FoodLevelChangedListener(NamespacedKey disabled, NamespacedKey hideMessage) {
        this.keyBetterFoodDisabled = disabled;
        this.keyBetterFoodHideMessage = hideMessage;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChanged(FoodLevelChangeEvent event) {

        if (!(event.getEntity() instanceof Player player))
            return;

        //Only acts on events where hunger is being lowered
        int playerFoodLevel = player.getFoodLevel();
        int newFoodLevel = event.getFoodLevel();
        if (newFoodLevel >= playerFoodLevel)
            return;

        if (!player.hasPermission("betterfood.user"))
            return;

        PersistentDataContainer playerData = player.getPersistentDataContainer();
        if (playerData.has(keyBetterFoodDisabled, PersistentDataType.BYTE))
            return;

        //Determines which index of FOODS_ORDERED_BY_HUNGER_RESTORED to start iterating from based on hunger level
        if (newFoodLevel < 0 || newFoodLevel >= MAX_HUNGER) return;
        int indexOffset = HUNGER_TO_FOOD_INDEX_OFFSET[newFoodLevel];

        PlayerInventory playerInventory = player.getInventory();

        //Iterates the Player inventory once creating a map as follows:
        //Key: ItemStack Material
        //Value: slot + ItemStack pairs for all matching slots in ascending inventory order
        //Used to restrict further inventory iterations to only the ItemStacks relevant to the searched Food
        Map<Material, List<InventoryFoodCandidate>> inventoryMap = new HashMap<>();
        for (int slot = 0; slot < playerInventory.getSize(); slot++) {
            ItemStack item = playerInventory.getItem(slot);
            if (item == null)
                continue;
            Material itemMaterial = item.getType();
            //Foods ordered before indexOffset are never searched below, so skip bucketing them
            Integer foodIndex = MATERIAL_TO_FOOD_INDEX.get(itemMaterial);
            if (foodIndex == null || foodIndex < indexOffset)
                continue;
            inventoryMap.computeIfAbsent(itemMaterial, ignored -> new ArrayList<>())
                    .add(new InventoryFoodCandidate(slot, item));
        }

        //The logic below searches for the first 'valid' food item in the Player inventory
        //Will iterate starting from the position of the first food in the array that would perfectly fill the hunger bar
        //Will continue iterating along the array in the direction of foods that are less nourishing until Food is found
        for (int i = indexOffset; i < FOODS_ORDERED_BY_HUNGER_RESTORED.length; i++) {

            Food food = FOODS_ORDERED_BY_HUNGER_RESTORED[i];
            Material foodMaterial = food.getFoodMaterial();

            List<InventoryFoodCandidate> inventoryFoodItems = inventoryMap.get(foodMaterial);
            if (inventoryFoodItems == null)
                continue;

            InventoryFoodCandidate validFood = inventoryFoodItems.stream()
                    .filter(candidate -> isUnmodifiedVanillaItem(candidate.itemStack()))
                    .findFirst()
                    .orElse(null);
            if (validFood == null)
                continue;

            //Restricts max and min hunger/saturation values following MC logic
            int newHungerLevel = Math.min(newFoodLevel + food.getHungerRestored(), MAX_HUNGER);
            float newSaturationLevel = Math.max(player.getSaturation() + food.getSaturationRestored(), 0);
            newSaturationLevel = Math.min(newSaturationLevel, newHungerLevel);

            if (!playerData.has(keyBetterFoodHideMessage, PersistentDataType.BYTE))
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText("You ate some " + foodMaterial));

            consumeFood(player, playerInventory, validFood, food);
            event.setFoodLevel(newHungerLevel);
            player.setSaturation(newSaturationLevel);

            break;
        }
    }

    /**
     * Compares the complete Bukkit item representation with a pristine stack of
     * the same material. Unlike a hand-maintained list of ItemMeta fields, this
     * also rejects new or opaque data components that Bukkit includes in item
     * similarity (including persistent custom data).
     */
    private static boolean isUnmodifiedVanillaItem(ItemStack itemStack) {
        return new ItemStack(itemStack.getType()).isSimilar(itemStack);
    }

    private static void consumeFood(Player player,
                                    PlayerInventory playerInventory,
                                    InventoryFoodCandidate foodCandidate,
                                    Food food) {
        ItemStack foodItem = foodCandidate.itemStack();
        int remainingFood = foodItem.getAmount() - 1;
        Material remainder = food.getRemainderMaterial();
        boolean returnsBowl = remainder != null;

        if (remainingFood <= 0) {
            playerInventory.setItem(
                    foodCandidate.slot(),
                    returnsBowl ? new ItemStack(remainder) : null
            );
            return;
        }

        foodItem.setAmount(remainingFood);
        // Defensive write-back: getItem() returning a live mirror is a CraftBukkit implementation detail, not an API guarantee.
        playerInventory.setItem(foodCandidate.slot(), foodItem);
        if (!returnsBowl)
            return;

        Map<Integer, ItemStack> overflow = playerInventory.addItem(new ItemStack(remainder));
        overflow.values().forEach(item ->
                player.getWorld().dropItemNaturally(player.getLocation(), item));
    }

    //Gets the index of the next Food in the target array with a different Hunger Restoration value
    private static int getNextHungerStartIndex(int currentIndex) {

        if (currentIndex < 0 || currentIndex >= FOODS_ORDERED_BY_HUNGER_RESTORED.length)
            throw new IllegalStateException("currentIndex out of bounds: " + currentIndex);

        int currentHungerRestored = FOODS_ORDERED_BY_HUNGER_RESTORED[currentIndex].getHungerRestored();

        for (int i = currentIndex + 1; i < FOODS_ORDERED_BY_HUNGER_RESTORED.length; i++) {

            int nextHungerRestoreAmount = FOODS_ORDERED_BY_HUNGER_RESTORED[i].getHungerRestored();
            if (nextHungerRestoreAmount == currentHungerRestored)
                continue;

            return i;
        }

        return currentIndex;
    }

    private record InventoryFoodCandidate(int slot, ItemStack itemStack) {
    }
}
