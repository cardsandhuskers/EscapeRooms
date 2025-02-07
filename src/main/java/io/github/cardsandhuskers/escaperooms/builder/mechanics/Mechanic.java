package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class Mechanic implements ConfigurationSerializable {
    protected UUID mechanicID;
    protected Level level;

    /**
     * Constructor, should be called by the constructor that makes a brand new mechanic
     */
    public Mechanic() {
        generateUUID();
    }

    /**
     * Generates the settings menu for the mechanic
     * @param player - player that's opening the inventory
     * @return - Inventory object that can be opened for the player
     */
    public abstract Inventory generateMechanicSettingsMenu(Player player);

    /**
     * Gets the lore for the mechanic item in the level editor inventory
     * @return - Component list for the item's lore
     */
    public abstract List<Component> getLore();

    /**
     * handles the click event for the mechanic's settings menu
     * @param e - clickEvent
     * @param editorGUIHandler - editorguihandler object to handle opening player inventories and such
     */
    public abstract void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler);

    /**
     * handle game time events (listeners pass through this to the mechanic
     * @param e
     */
    public abstract void eventHandler(TeamInstance teamInstance, Event e);

    /**
     * Implement behaviors for when the level starts
     * e.g. giving items or teleports
     */
    public abstract void levelStartExecution(TeamInstance teamInstance);

    /**
     * Converts the Mechanic's data into text in a HashMap structure to write to the config
     * @return
     */
    @Override
    public abstract Map<String, Object> serialize();

    public UUID getID() {
        return mechanicID;
    }

    public Level getLevel() {
        return level;
    }

    private void generateUUID() {
        mechanicID = UUID.randomUUID();
    }

    /**
     * Deletes the mechanic from memory and the config file
     */
    public void delete() {
        EscapeRooms plugin = EscapeRooms.getPlugin();

        File file = new File(plugin.getDataFolder(), level.getName() + ".yml");
        FileConfiguration levelConfig = YamlConfiguration.loadConfiguration(file);

        // Check if the mechanic exists
        String mechanicPath = "mechanics." + mechanicID;
        if (levelConfig.contains(mechanicPath)) {
            // Remove the mechanic
            levelConfig.set(mechanicPath, null);

            // Save the updated config
            try {levelConfig.save(file);} catch (Exception e) {e.printStackTrace();}

            plugin.getLogger().info("Mechanic with ID '" + mechanicID + "' has been removed from the config.");
        } else {
            plugin.getLogger().info("Mechanic with ID '" + mechanicID + "' does not exist in the config.");
        }

        level.removeMechanic(this);
    }

    /**
     * Generates and opens a menu to let the player confirm if they want to delete the mechanic or not
     * @param player
     */
    public void openDeleteMenu(Player player) {
        EscapeRooms plugin = EscapeRooms.getPlugin();
        Inventory deleteLevelMenu = Bukkit.createInventory(player, 18, Component.text("Delete Mechanic?").decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GREEN));

        deleteLevelMenu.setItem(4, createIDItem(mechanicID, MechanicMapper.getMechMaterial(this.getClass())));

        ItemStack yes = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.displayName(Component.text("Yes").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        yes.setItemMeta(yesMeta);
        deleteLevelMenu.setItem(11, yes);

        ItemStack no = new ItemStack(Material.RED_CONCRETE);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.displayName(Component.text("No").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        no.setItemMeta(noMeta);
        deleteLevelMenu.setItem(15, no);

        player.openInventory(deleteLevelMenu);
    }

    /**
     * serialize itemStack into a string
     * @param itemStack - stack to serialize
     * @return serialized string
     */
    public static String serializeItemStack(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", itemStack);
        return config.saveToString();
    }

    /**
     * deserialize itemStack
     * @param yamlString - String to deserialize
     * @return - deserialized ItemStack object
     */
    public static ItemStack deserializeItemStack(String yamlString) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new java.io.StringReader(yamlString));
        return config.getItemStack("item");
    }

    /**
     * Creates the item at the top of the mechanic menu, contains the ID of the mechanic so that the mechanic the user
     * is working with can be retreived
     * @param mechanicID
     * @param mat
     * @return
     */
    public static ItemStack createIDItem(UUID mechanicID, Material mat) {
        EscapeRooms plugin = EscapeRooms.getPlugin();
        ItemStack title = new ItemStack(mat);
        ItemMeta titleMeta = title.getItemMeta();
        titleMeta.displayName(Component.text("").decoration(TextDecoration.ITALIC, false));
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        PersistentDataContainer container = titleMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, mechanicID.toString());
        title.setItemMeta(titleMeta);

        return title;
    }

    /**
     * Pulls the UUID from an ID item
     * @param clickedItem
     * @return
     */
    public static UUID getUUIDFromItem(ItemStack clickedItem) {
        EscapeRooms plugin = EscapeRooms.getPlugin();
        ItemMeta itemMeta = clickedItem.getItemMeta();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        String idString = container.get(namespacedKey, PersistentDataType.STRING);
        if(idString != null) {
            return UUID.fromString(idString);
        } else {
            return null;
        }
    }

    /**
     * embeds the UUID in the item passed in
     * @param itemMeta - item data to embed UUID into
     * @param id - UUID to embed
     */
    public static void embedUUID(ItemMeta itemMeta, UUID id) {
        EscapeRooms plugin = EscapeRooms.getPlugin();

        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, id.toString());

    }

    /**
     * Creates the item for the level editor page that will have the data about the mechanic
     * @return
     */
    public ItemStack createEditorPageItem() {
        Material mat = MechanicMapper.getMechMaterial(this.getClass());
        ItemStack mechanicStack = new ItemStack(mat);

        ItemMeta mechanicMeta = mechanicStack.getItemMeta();
        Mechanic.embedUUID(mechanicMeta, mechanicID);
        mechanicMeta.displayName(Component.text(MechanicMapper.getMechName(this.getClass())).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        mechanicMeta.lore(getLore());
        mechanicStack.setItemMeta(mechanicMeta);

        return mechanicStack;
    }

    /**
     *
     * @param mat - material to use
     * @param count - number of items to put in the stack
     * @param name - custom name for the item, use null for default name
     * @param lore - lore for the item, use null for no lore
     * @return
     */
    public static ItemStack createItem( Material mat, int count, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat, count);
        ItemMeta itemMeta = item.getItemMeta();

        if(name != null) {
            itemMeta.displayName(name);
        }
        if(lore != null) {
            itemMeta.lore(lore);
        }

        item.setItemMeta(itemMeta);

        return item;

    }

}
