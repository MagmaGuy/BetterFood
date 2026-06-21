package com.magmaguy.betterfood;

import org.bukkit.Material;

import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public record Food(Material foodMaterial, int hungerRestored, float saturationRestored) {
    private static final Map<Material, Food> FOOD_BY_MATERIAL = new EnumMap<>(Material.class);
    private static final Set<Material> EXCLUDED_FOOD = EnumSet.of(
            Material.BEEF,
            Material.CAKE,
            Material.CHICKEN,
            Material.CHORUS_FRUIT,
            Material.COD,
            Material.ENCHANTED_GOLDEN_APPLE,
            Material.GOLDEN_APPLE,
            Material.HONEY_BOTTLE,
            Material.MUTTON,
            Material.POISONOUS_POTATO,
            Material.PORKCHOP,
            Material.PUFFERFISH,
            Material.RABBIT,
            Material.ROTTEN_FLESH,
            Material.SALMON,
            Material.SPIDER_EYE,
            Material.SUSPICIOUS_STEW,
            Material.TROPICAL_FISH
    );
    private static final Comparator<Food> BEST_FIT_COMPARATOR = Comparator
            .comparingInt(Food::hungerRestored)
            .thenComparing(Food::saturationRestored)
            .reversed();

    static {
        register(new Food(Material.APPLE, 4, 2.4f));
        register(new Food(Material.BAKED_POTATO, 5, 6.0f));
        register(new Food(Material.BEETROOT, 1, 1.2f));
        register(new Food(Material.BEETROOT_SOUP, 6, 7.2f));
        register(new Food(Material.BREAD, 5, 6.0f));
        register(new Food(Material.CARROT, 3, 3.6f));
        register(new Food(Material.COOKED_BEEF, 8, 12.8f));
        register(new Food(Material.COOKED_CHICKEN, 6, 7.2f));
        register(new Food(Material.COOKED_COD, 5, 6.0f));
        register(new Food(Material.COOKED_MUTTON, 6, 9.6f));
        register(new Food(Material.COOKED_PORKCHOP, 8, 12.8f));
        register(new Food(Material.COOKED_RABBIT, 5, 6.0f));
        register(new Food(Material.COOKED_SALMON, 6, 9.6f));
        register(new Food(Material.COOKIE, 2, 0.4f));
        register(new Food(Material.DRIED_KELP, 1, 0.6f));
        register(new Food(Material.GLOW_BERRIES, 2, 0.4f));
        register(new Food(Material.GOLDEN_CARROT, 6, 14.4f));
        register(new Food(Material.MELON_SLICE, 2, 1.2f));
        register(new Food(Material.MUSHROOM_STEW, 6, 7.2f));
        register(new Food(Material.POTATO, 1, 0.6f));
        register(new Food(Material.PUMPKIN_PIE, 8, 4.8f));
        register(new Food(Material.RABBIT_STEW, 10, 12.0f));
        register(new Food(Material.SWEET_BERRIES, 2, 0.4f));
    }

    private static void register(Food food) {
        FOOD_BY_MATERIAL.put(food.foodMaterial(), food);
    }

    public float effectiveRestoration() {
        return hungerRestored + saturationRestored;
    }

    public static Food get(Material material) {
        return FOOD_BY_MATERIAL.get(material);
    }

    public static Collection<Food> getSupportedFood() {
        return FOOD_BY_MATERIAL.values();
    }

    public static Food findBestFood(int currentFoodLevel) {
        int missingHunger = 20 - currentFoodLevel;
        if (missingHunger <= 0) {
            return null;
        }

        List<Food> underOrExact = FOOD_BY_MATERIAL.values().stream()
                .filter(food -> food.hungerRestored() <= missingHunger)
                .sorted(BEST_FIT_COMPARATOR)
                .toList();
        if (!underOrExact.isEmpty()) {
            return underOrExact.getFirst();
        }

        return FOOD_BY_MATERIAL.values().stream()
                .sorted(Comparator.comparingInt((Food food) -> food.hungerRestored() - missingHunger)
                        .thenComparing(Food::saturationRestored).reversed())
                .findFirst()
                .orElse(null);
    }

    public static boolean isExcluded(Material material) {
        return EXCLUDED_FOOD.contains(material);
    }

    public static void logCoverageAudit(Logger logger) {
        List<String> unknownFoods = EnumSet.allOf(Material.class).stream()
                .filter(material -> !material.isLegacy())
                .filter(Material::isEdible)
                .filter(material -> !FOOD_BY_MATERIAL.containsKey(material))
                .filter(material -> !EXCLUDED_FOOD.contains(material))
                .map(Enum::name)
                .sorted()
                .collect(Collectors.toList());
        if (!unknownFoods.isEmpty()) {
            logger.warning("[BetterFood] Unknown edible materials detected and not yet classified: " + String.join(", ", unknownFoods));
        }
    }
}
