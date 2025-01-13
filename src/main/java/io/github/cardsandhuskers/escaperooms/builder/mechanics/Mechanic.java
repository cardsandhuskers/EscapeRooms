package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

    public UUID getID() {
        return mechanicID;
    }

    public Level getLevel() {
        return level;
    }

    private void generateUUID() {
        mechanicID = UUID.randomUUID();
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
}
