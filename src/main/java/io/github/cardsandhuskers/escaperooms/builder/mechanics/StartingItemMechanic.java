package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class StartingItemMechanic extends Mechanic{

    private ItemStack item = null;
    public StartingItemMechanic(Level level) {
        super();
        this.level = level;
    }

    public StartingItemMechanic(String mechanicID, ItemStack item, Level level) {
        this.mechanicID = UUID.fromString(mechanicID);
        this.level = level;
        setItem(item);

    }

    @Override
    public HashMap<String, Object> writeData() {
        HashMap<String, Object> mechanicEntry = new HashMap<>();

        // Prepare attributes map
        Map<String, Object> attributes = new HashMap<>();
        if (getItem() != null) {
            // Serialize the ItemStack
            attributes.put("item", serializeItemStack(getItem()));
        }

        // Set other attributes like "type"
        attributes.put("type", "Give Item on Spawn");

        // Add the mechanic ID and its attributes to the mechanic entry
        mechanicEntry.put(String.valueOf(getID()), attributes);

        return mechanicEntry;
    }


    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {
        EscapeRooms plugin = JavaPlugin.getPlugin(EscapeRooms.class);
        Inventory mechanicInv = Bukkit.createInventory(player, 27, Component.text("Mechanic: Give Item on Spawn")
                .color(NamedTextColor.BLUE));

        ItemStack title = new ItemStack(Material.BOOK);
        ItemMeta titleMeta = title.getItemMeta();
        titleMeta.displayName(Component.text("").decoration(TextDecoration.ITALIC, false));

        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        PersistentDataContainer container = titleMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, mechanicID.toString());

        ItemStack explainer = new ItemStack(Material.ARROW);
        ItemMeta explainerMeta = explainer.getItemMeta();
        explainerMeta.displayName(Component.text(">>>").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
        List<Component> explanationLore = List.of(Component.text("Place an item in the orange"),
                                                  Component.text("concrete to select it as the"),
                                                  Component.text("item to give to players."));
        explainerMeta.lore(explanationLore);
        explainer.setItemMeta(explainerMeta);
        mechanicInv.setItem(11, explainer);

        ItemStack placementPoint = new ItemStack(Material.ORANGE_CONCRETE);
        ItemMeta placementPointMeta = placementPoint.getItemMeta();
        placementPointMeta.displayName(Component.text("Place Item Here!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD));
        placementPoint.setItemMeta(placementPointMeta);
        mechanicInv.setItem(12, placementPoint);

        ItemStack currExplainer = new ItemStack(Material.ARROW);
        ItemMeta currExplainerMeta = currExplainer.getItemMeta();
        currExplainerMeta.displayName(Component.text("Current Item:").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
        List<Component> currExplainerLore = List.of(Component.text("The item on the right is what"),
                                                    Component.text("is currently stored to give "),
                                                    Component.text("to players."));
        currExplainerMeta.lore(currExplainerLore);
        currExplainer.setItemMeta(currExplainerMeta);
        mechanicInv.setItem(14, currExplainer);

        if(getItem() == null) {
            ItemStack holderItem = new ItemStack(Material.GLASS_PANE);
            ItemMeta holderItemMeta = holderItem.getItemMeta();
            holderItemMeta.displayName(Component.text("None!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE));
            holderItem.setItemMeta(holderItemMeta);
            mechanicInv.setItem(15, holderItem);
        } else {
            mechanicInv.setItem(15, getItem());
        }

        title.setItemMeta(titleMeta);
        mechanicInv.setItem(4, title);

        ItemStack back = new ItemStack(Material.RED_CONCRETE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);
        mechanicInv.setItem(26, back);

        return mechanicInv;
    }

    public void setItem(ItemStack item) {
        this.item = item.clone();
    }

    public ItemStack getItem() {
        if(item != null) {
            return item.clone();
        }
        return null;

    }
}
