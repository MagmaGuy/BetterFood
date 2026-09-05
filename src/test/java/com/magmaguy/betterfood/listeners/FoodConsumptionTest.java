package com.magmaguy.betterfood.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoodConsumptionTest {
    private static final NamespacedKey DISABLED = new NamespacedKey("betterfood", "enabled");
    private static final NamespacedKey HIDE_MESSAGE = new NamespacedKey("betterfood", "showmessage");
    private ServerMock server;
    private FeedbackPlayer player;
    private PluginMock plugin;

    @BeforeEach
    void registerListener() {
        server = MockBukkit.mock();
        plugin = MockBukkit.createMockPlugin();
        player = new FeedbackPlayer(server);
        server.addPlayer(player);
        player.addAttachment(plugin, "betterfood.user", true);
        player.setFoodLevel(13);
        player.setSaturation(0F);
        server.getPluginManager().registerEvents(new FoodLevelChangedListener(DISABLED, HIDE_MESSAGE), plugin);
    }

    @AfterEach
    void closeServer() {
        MockBukkit.unmock();
    }

    @Test
    void hungerReductionConsumesOneBestFittingFoodAndRestoresSaturation() {
        player.getInventory().setItem(0, new ItemStack(Material.BREAD, 2));
        player.getInventory().setItem(1, new ItemStack(Material.COOKED_BEEF, 2));
        player.getInventory().setItem(2, new ItemStack(Material.RABBIT_STEW));

        FoodLevelChangeEvent event = dispatch(12, false);

        assertEquals(20, event.getFoodLevel());
        assertEquals(12.8F, player.getSaturation(), 0.001F);
        assertEquals(2, player.getInventory().getItem(0).getAmount());
        assertEquals(1, player.getInventory().getItem(1).getAmount());
        assertEquals(new ItemStack(Material.RABBIT_STEW), player.getInventory().getItem(2));
        assertEquals(List.of(ChatMessageType.ACTION_BAR), player.channels);
        assertEquals(1, player.messages.size());
        assertTrue(player.messages.getFirst().contains("COOKED_BEEF"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"name", "persistent-data"})
    void modifiedFoodIsPreservedWhileTheNextPristineStackIsConsumed(String modification) {
        ItemStack modified = new ItemStack(Material.COOKED_BEEF);
        modified.editMeta(meta -> {
            if (modification.equals("name")) meta.setDisplayName("Player-owned food");
            else meta.getPersistentDataContainer().set(new NamespacedKey("test", "owned"),
                    PersistentDataType.BYTE, (byte) 1);
        });
        player.getInventory().setItem(0, modified);
        player.getInventory().setItem(1, new ItemStack(Material.COOKED_BEEF, 2));

        assertEquals(20, dispatch(12, false).getFoodLevel());
        assertEquals(modified, player.getInventory().getItem(0));
        assertEquals(1, player.getInventory().getItem(1).getAmount());
    }

    @Test
    void consumingLastStewReturnsItsBowlInTheSameSlot() {
        player.setFoodLevel(15);
        player.getPersistentDataContainer().set(HIDE_MESSAGE, PersistentDataType.BYTE, (byte) 1);
        player.getInventory().setItem(3, new ItemStack(Material.MUSHROOM_STEW));

        assertEquals(20, dispatch(14, false).getFoodLevel());
        assertEquals(new ItemStack(Material.BOWL), player.getInventory().getItem(3));
        assertTrue(player.messages.isEmpty(), "Hiding feedback must not disable consumption");
    }

    @ParameterizedTest
    @ValueSource(strings = {"permission", "opt-out", "cancelled", "increasing", "excluded-food"})
    void ineligibleEventsLeaveInventoryAndSaturationUnchanged(String reason) {
        ItemStack food = new ItemStack(reason.equals("excluded-food") ? Material.ROTTEN_FLESH : Material.COOKED_BEEF, 2);
        player.getInventory().setItem(0, food.clone());
        if (reason.equals("permission")) player.addAttachment(plugin, "betterfood.user", false);
        if (reason.equals("opt-out")) player.getPersistentDataContainer()
                .set(DISABLED, PersistentDataType.BYTE, (byte) 1);
        int requested = reason.equals("increasing") ? 14 : 12;

        FoodLevelChangeEvent event = dispatch(requested, reason.equals("cancelled"));

        assertEquals(requested, event.getFoodLevel());
        assertEquals(food, player.getInventory().getItem(0));
        assertEquals(0F, player.getSaturation());
        assertTrue(player.messages.isEmpty(), "Rejected consumption must not emit feedback");
    }

    // MockBukkit's Spigot bridge discards ChatMessageType. Record the real
    // listener's call at that Bukkit interface, without replacing food logic.
    private static final class FeedbackPlayer extends PlayerMock {
        private final List<ChatMessageType> channels = new ArrayList<>();
        private final List<String> messages = new ArrayList<>();
        private final org.bukkit.entity.Player.Spigot feedback = new org.bukkit.entity.Player.Spigot() {
            @Override
            public void sendMessage(ChatMessageType position, BaseComponent... components) {
                channels.add(position);
                messages.add(BaseComponent.toPlainText(components));
            }
        };

        private FeedbackPlayer(ServerMock server) { super(server, "FoodTester"); }

        @Override
        public org.bukkit.entity.Player.Spigot spigot() { return feedback; }
    }

    private FoodLevelChangeEvent dispatch(int foodLevel, boolean cancelled) {
        FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, foodLevel, null);
        event.setCancelled(cancelled);
        server.getPluginManager().callEvent(event);
        return event;
    }
}
