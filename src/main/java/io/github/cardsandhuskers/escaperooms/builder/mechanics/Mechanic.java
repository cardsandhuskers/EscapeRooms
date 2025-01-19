package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public abstract class Mechanic {
    protected UUID mechanicID;
    protected Level level;

    public Mechanic() {
        generateUUID();
    }

    public abstract Map<String, Object> getData();

    public abstract Inventory generateMechanicSettingsMenu(Player player);
    public abstract ItemStack createItem();

    public UUID getID() {
        return mechanicID;
    }

    public Level getLevel() {
        return level;
    }

    private void generateUUID() {
        mechanicID = UUID.randomUUID();
    }

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

    //stolen from ChatGPT
    // Serialize ItemStack to YAML string
    public static String serializeItemStack(ItemStack itemStack) {
        YamlConfiguration config = new YamlConfiguration();
        config.set("item", itemStack);
        return config.saveToString();
    }

    // Deserialize ItemStack from YAML string
    public static ItemStack deserializeItemStack(String yamlString) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new java.io.StringReader(yamlString));
        return config.getItemStack("item");
    }

    public abstract void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler);

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

    public static void embedUUID(ItemMeta itemMeta, UUID id) {
        EscapeRooms plugin = EscapeRooms.getPlugin();

        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, id.toString());

    }
}
