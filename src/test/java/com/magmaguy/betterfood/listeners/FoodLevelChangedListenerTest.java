package com.magmaguy.betterfood.listeners;

import com.magmaguy.betterfood.BetterFood;
import com.magmaguy.betterfood.commands.BetterFoodCommand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class FoodLevelChangedListenerTest {

    private ServerMock server;
    private PluginMock keyPlugin;
    private BetterFood plugin;

    @BeforeEach
    void setUp() throws Exception {
        server = MockBukkit.mock();
        keyPlugin = MockBukkit.createMockPlugin("BetterFood");
        plugin = betterFoodWithKeys(keyPlugin);
    }

    @AfterEach
    void tearDown() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    void hungerDropConsumesBestAvailablePlainFood() {
        PlayerMock player = playerWithUserPermission();
        player.setFoodLevel(16);
        player.setSaturation(0.0f);
        player.getInventory().setItem(0, new ItemStack(Material.COOKED_BEEF, 2));

        FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, 12, null);

        new FoodLevelChangedListener(plugin).onFoodLevelChanged(event);

        assertEquals(20, event.getFoodLevel());
        assertEquals(12.8f, player.getSaturation(), 0.0001f);
        assertEquals(1, player.getInventory().getItem(0).getAmount());
    }

    @Test
    void customNamedFoodIsNotConsumedAutomatically() {
        PlayerMock player = playerWithUserPermission();
        player.setFoodLevel(16);
        player.setSaturation(0.0f);
        ItemStack customSteak = new ItemStack(Material.COOKED_BEEF, 2);
        ItemMeta itemMeta = customSteak.getItemMeta();
        itemMeta.setDisplayName("Chef's Special");
        customSteak.setItemMeta(itemMeta);
        player.getInventory().setItem(0, customSteak);

        FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, 12, null);

        new FoodLevelChangedListener(plugin).onFoodLevelChanged(event);

        assertEquals(12, event.getFoodLevel());
        assertEquals(0.0f, player.getSaturation(), 0.0001f);
        assertEquals(2, player.getInventory().getItem(0).getAmount());
    }

    @Test
    void disabledPlayerDataSkipsAutomaticEating() {
        PlayerMock player = playerWithUserPermission();
        player.setFoodLevel(16);
        player.setSaturation(0.0f);
        player.getPersistentDataContainer().set(plugin.getKeyDisabled(), PersistentDataType.BYTE, (byte) 0);
        player.getInventory().setItem(0, new ItemStack(Material.COOKED_BEEF, 2));

        FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, 12, null);

        new FoodLevelChangedListener(plugin).onFoodLevelChanged(event);

        assertEquals(12, event.getFoodLevel());
        assertEquals(0.0f, player.getSaturation(), 0.0001f);
        assertEquals(2, player.getInventory().getItem(0).getAmount());
    }

    @Test
    void commandTogglesAutomaticEatingPersistentData() {
        PlayerMock player = playerWithUserPermission();
        BetterFoodCommand commandExecutor = new BetterFoodCommand(plugin);
        Command command = simpleCommand("betterfood");

        assertTrue(commandExecutor.onCommand(player, command, "betterfood", new String[0]));
        assertTrue(player.getPersistentDataContainer().has(plugin.getKeyDisabled(), PersistentDataType.BYTE));
        assertEquals("[BF] Automatic Eating: OFF", player.nextMessage());

        assertTrue(commandExecutor.onCommand(player, command, "betterfood", new String[]{"toggleEating"}));
        assertFalse(player.getPersistentDataContainer().has(plugin.getKeyDisabled(), PersistentDataType.BYTE));
        assertEquals("[BF] Automatic Eating: ON", player.nextMessage());
    }

    private PlayerMock playerWithUserPermission() {
        PlayerMock player = server.addPlayer("Taster");
        player.addAttachment(keyPlugin, "betterfood.user", true);
        return player;
    }

    private static Command simpleCommand(String name) {
        return new Command(name) {
            @Override
            public boolean execute(CommandSender sender, String commandLabel, String[] args) {
                return false;
            }
        };
    }

    private static BetterFood betterFoodWithKeys(PluginMock keyPlugin) throws Exception {
        BetterFood plugin = allocate(BetterFood.class);
        setField(plugin, "keyDisabled", new NamespacedKey(keyPlugin, "enabled"));
        setField(plugin, "keyHideMessage", new NamespacedKey(keyPlugin, "showmessage"));
        setField(plugin, "keyMode", new NamespacedKey(keyPlugin, "mode"));
        return plugin;
    }

    private static <T> T allocate(Class<T> type) throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        return type.cast(unsafe.allocateInstance(type));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
