package com.magmaguy.betterfood;

import lombok.Getter;
import org.bukkit.Material;

import java.util.*;
import java.util.logging.Logger;

public final class Food {

    // Foods with special consumption effects (and raw meats/fish) are intentionally excluded; logCoverageAudit logs the live exclusion list at startup.

    private static final Set<Food> FOOD = Set.of(
            new Food(Material.APPLE, 4, 2.4f),
            new Food(Material.BAKED_POTATO, 5, 6.0f),
            new Food(Material.BEETROOT, 1, 1.2f),
            new Food(Material.BEETROOT_SOUP, 6, 7.2f, Material.BOWL),
            new Food(Material.BREAD, 5, 6.0f),
            new Food(Material.CARROT, 3, 3.6f),
            new Food(Material.COOKED_BEEF, 8, 12.8f),
            new Food(Material.COOKED_CHICKEN, 6, 7.2f),
            new Food(Material.COOKED_COD, 5, 6.0f),
            new Food(Material.COOKED_MUTTON, 6, 9.6f),
            new Food(Material.COOKED_PORKCHOP, 8, 12.8f),
            new Food(Material.COOKED_RABBIT, 5, 6.0f),
            new Food(Material.COOKED_SALMON, 6, 9.6f),
            new Food(Material.COOKIE, 2, 0.4f),
            new Food(Material.DRIED_KELP, 1, 0.6f),
            new Food(Material.GLOW_BERRIES, 2, 0.4f),
            new Food(Material.GOLDEN_CARROT, 6, 14.4f),
            new Food(Material.MELON_SLICE, 2, 1.2f),
            new Food(Material.MUSHROOM_STEW, 6, 7.2f, Material.BOWL),
            new Food(Material.POTATO, 1, 0.6f),
            new Food(Material.PUMPKIN_PIE, 8, 4.8f),
            new Food(Material.RABBIT_STEW, 10, 12.0f, Material.BOWL),
            new Food(Material.SWEET_BERRIES, 2, 0.4f)
    );

    @Getter
    private final Material foodMaterial;
    @Getter
    private final int hungerRestored;
    @Getter
    private final float saturationRestored;
    @Getter
    private final Material remainderMaterial;

    private Food(Material foodMaterial, int hungerRestored, float saturationRestored) {
        this(foodMaterial, hungerRestored, saturationRestored, null);
    }

    private Food(Material foodMaterial, int hungerRestored, float saturationRestored, Material remainderMaterial) {
        this.foodMaterial = foodMaterial;
        this.hungerRestored = hungerRestored;
        this.saturationRestored = saturationRestored;
        this.remainderMaterial = remainderMaterial;
    }

    public static Set<Food> getFood() {
        return FOOD;
    }

    public static void logCoverageAudit(Logger logger) {
        List<String> missingFoods = Arrays.stream(Material.values())
                .filter(Material::isItem)
                .filter(Material::isEdible)
                .filter(material -> FOOD.stream().noneMatch(food -> food.getFoodMaterial() == material))
                .map(Material::name)
                .sorted()
                .toList();

        if (!missingFoods.isEmpty())
            logger.info("BetterFood is intentionally not auto-eating these food items: " + String.join(", ", missingFoods));
    }

}
