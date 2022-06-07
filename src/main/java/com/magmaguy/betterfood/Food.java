package com.magmaguy.betterfood;

import lombok.Getter;
import org.bukkit.Material;

import java.util.*;

public final class Food {

    /*Foods that have special effects when eaten that are not currently accounted for
        new Food(Material.CHORUS_FRUIT, 4, 2.4f)
        new Food(Material.ENCHANTED_GOLDEN_APPLE, 4, 9.6f)
        new Food(Material.GOLDEN_APPLE, 4, 9.6f)
        new Food(Material.HONEY_BOTTLE, 6, 1.2f)
        new Food(Material.POISONOUS_POTATO, 2, 1.2f)
        new Food(Material.PUFFERFISH, 1, 0.2f)
        new Food(Material.ROTTEN_FLESH, 4, 0.8f)
        new Food(Material.SPIDER_EYE, 2, 3.2f)
        new Food(Material.SUSPICIOUS_STEW, 6, 7.2f)
     */

    /*Other excluded foods
        new Food(Material.BEEF, 3, 1.8f)
        new Food(Material.COD, 2, 0.4f)
        new Food(Material.MUTTON, 2, 1.2f)
        new Food(Material.PORKCHOP, 1, 2.0f)
        new Food(Material.RABBIT, 3, 1.8f)
        new Food(Material.SALMON, 2, 0.4f)
        new Food(Material.TROPICAL_FISH, 1, 0.2f)
     */

    private static final Set<Food> FOOD = Set.of(
            new Food(Material.APPLE, 4, 2.4f),
            new Food(Material.BAKED_POTATO, 5, 6.0f),
            new Food(Material.BEETROOT, 1, 1.2f),
            new Food(Material.BEETROOT_SOUP, 6, 7.2f),
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
            new Food(Material.MUSHROOM_STEW, 6, 7.2f),
            new Food(Material.POTATO, 1, 0.6f),
            new Food(Material.PUMPKIN_PIE, 8, 4.8f),
            new Food(Material.RABBIT_STEW, 10, 12.0f),
            new Food(Material.SWEET_BERRIES, 2, 0.4f)
    );

    @Getter
    private final Material foodMaterial;
    @Getter
    private final int hungerRestored;
    @Getter
    private final float saturationRestored;
    @Getter
    private final float effectiveRestoration;

    private Food(Material foodMaterial, int hungerRestored, float saturationRestored) {
        this.foodMaterial = foodMaterial;
        this.hungerRestored = hungerRestored;
        this.saturationRestored = saturationRestored;
        this.effectiveRestoration = hungerRestored + saturationRestored;
    }

    public static Set<Food> getFood() {
        return FOOD;
    }

}
