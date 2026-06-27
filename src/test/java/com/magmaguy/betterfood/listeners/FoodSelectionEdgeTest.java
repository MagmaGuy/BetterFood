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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodSelectionEdgeTest {
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
    void tinyHungerDropConsumesSmallFoodBeforeLargeFood() {
        PlayerMock player = playerWithUserPermission();
        player.setFoodLevel(20);
        player.setSaturation(0.0f);
        player.getInventory().setItem(0, new ItemStack(Material.COOKED_BEEF, 1));
        player.getInventory().setItem(1, new ItemStack(Material.BEETROOT, 2));

        FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, 19, null);

        new FoodLevelChangedListener(plugin).onFoodLevelChanged(event);

        assertEquals(20, event.getFoodLevel());
        assertEquals(1.2f, player.getSaturation(), 0.0001f);
        assertEquals(1, player.getInventory().getItem(0).getAmount());
        assertEquals(1, player.getInventory().getItem(1).getAmount());
    }

    @Test
    void loreTaggedFoodIsNotConsumedAutomatically() {
        PlayerMock player = playerWithUserPermission();
        player.setFoodLevel(16);
        player.setSaturation(0.0f);
        ItemStack customBread = new ItemStack(Material.BREAD, 2);
        ItemMeta itemMeta = customBread.getItemMeta();
        itemMeta.setLore(List.of("Quest item"));
        customBread.setItemMeta(itemMeta);
        player.getInventory().setItem(0, customBread);

        FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, 12, null);

        new FoodLevelChangedListener(plugin).onFoodLevelChanged(event);

        assertEquals(12, event.getFoodLevel());
        assertEquals(0.0f, player.getSaturation(), 0.0001f);
        assertEquals(2, player.getInventory().getItem(0).getAmount());
    }

    @Test
    void playersWithoutPermissionAndNonHungerDropsAreIgnored() {
        PlayerMock player = server.addPlayer("NoSnack");
        player.setFoodLevel(16);
        player.getInventory().setItem(0, new ItemStack(Material.COOKED_BEEF, 2));
        FoodLevelChangedListener listener = new FoodLevelChangedListener(plugin);

        FoodLevelChangeEvent noPermissionDrop = new FoodLevelChangeEvent(player, 12, null);
        listener.onFoodLevelChanged(noPermissionDrop);

        player.addAttachment(keyPlugin, "betterfood.user", true);
        FoodLevelChangeEvent hungerGain = new FoodLevelChangeEvent(player, 18, null);
        listener.onFoodLevelChanged(hungerGain);

        assertEquals(12, noPermissionDrop.getFoodLevel());
        assertEquals(18, hungerGain.getFoodLevel());
        assertEquals(2, player.getInventory().getItem(0).getAmount());
    }

    @Test
    void toggleMessageCommandStoresAndClearsNotificationPreference() {
        PlayerMock player = playerWithUserPermission();
        BetterFoodCommand commandExecutor = new BetterFoodCommand(plugin);
        Command command = simpleCommand("betterfood");

        assertTrue(commandExecutor.onCommand(player, command, "betterfood", new String[]{"toggleMessage"}));
        assertTrue(player.getPersistentDataContainer().has(plugin.getKeyHideMessage(), PersistentDataType.BYTE));
        assertEquals("[BF] No longer showing food consumption notifications", player.nextMessage());

        assertTrue(commandExecutor.onCommand(player, command, "betterfood", new String[]{"toggleMessage"}));
        assertFalse(player.getPersistentDataContainer().has(plugin.getKeyHideMessage(), PersistentDataType.BYTE));
        assertEquals("[BF] Now showing food consumption notifications", player.nextMessage());
    }

    @Test
    void hiddenMessagePreferenceStillConsumesFoodButSuppressesActionBarNotice() {
        PlayerMock player = playerWithUserPermission();
        player.setFoodLevel(16);
        player.setSaturation(0.0f);
        player.getPersistentDataContainer().set(plugin.getKeyHideMessage(), PersistentDataType.BYTE, (byte) 0);
        player.getInventory().setItem(0, new ItemStack(Material.COOKED_CHICKEN, 1));

        FoodLevelChangeEvent event = new FoodLevelChangeEvent(player, 12, null);

        new FoodLevelChangedListener(plugin).onFoodLevelChanged(event);

        assertEquals(18, event.getFoodLevel());
        assertEquals(0, player.getInventory().getItem(0).getAmount());
    }

    @Test
    void commandPermissionMatrixCoversConsoleUserAndAdminBranchesWithoutCallingExternalUpdates() {
        BetterFoodCommand commandExecutor = new BetterFoodCommand(plugin);
        Command command = simpleCommand("betterfood");
        PlayerMock noUserPermission = server.addPlayer("NoPerm");

        assertTrue(commandExecutor.onCommand(server.getConsoleSender(), command, "betterfood", new String[0]));
        assertEquals("[BF] Use /betterfood downloadpluginupdate to check for plugin updates.", server.getConsoleSender().nextMessage());

        assertTrue(commandExecutor.onCommand(noUserPermission, command, "betterfood", new String[0]));
        assertEquals("[BF] You do not have permission to change BetterFood settings.", noUserPermission.nextMessage());

        assertTrue(commandExecutor.onCommand(noUserPermission, command, "betterfood", new String[]{"downloadallcontent"}));
        assertEquals("[BF] You do not have permission to update BetterFood.", noUserPermission.nextMessage());

        assertTrue(commandExecutor.onCommand(server.getConsoleSender(), command, "betterfood", new String[]{"downloadallcontent"}));
        assertEquals("[BF] BetterFood does not have content packs. Use /betterfood setup for plugin updates and settings.", server.getConsoleSender().nextMessage());

        PlayerMock admin = server.addPlayer("Admin");
        admin.addAttachment(keyPlugin, "betterfood.admin", true);

        assertTrue(commandExecutor.onCommand(admin, command, "betterfood", new String[]{"downloadallcontent"}));
        assertEquals("[BF] BetterFood does not have content packs. Use /betterfood setup for plugin updates and settings.", admin.nextMessage());
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
