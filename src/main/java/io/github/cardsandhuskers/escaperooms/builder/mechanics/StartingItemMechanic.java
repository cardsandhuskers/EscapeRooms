package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class StartingItemMechanic extends Mechanic{

    private ItemStack item = null;
    public StartingItemMechanic(Level level) {
        super();
        this.level = level;
    }

    public StartingItemMechanic(String mechanicID, Level level, ConfigurationSection attributes) {
        this.mechanicID = UUID.fromString(mechanicID);
        this.level = level;

        String itemString = attributes.getString("item");
        ItemStack item = null;

        // Deserialize the item if it exists
        if (itemString != null && !itemString.isEmpty()) {
            item = Mechanic.deserializeItemStack(itemString);
        }

        setItem(item);

    }

    @Override
    public Map<String, Object> getData() {

        // Prepare attributes map
        Map<String, Object> attributes = new HashMap<>();
        if (getItem() != null) {
            // Serialize the ItemStack
            attributes.put("item", serializeItemStack(getItem()));
        }

        // Set other attributes like "type"
        attributes.put("type", MechanicMapper.getMechName(this.getClass()));

        return attributes;
    }

    @Override
    public ItemStack createItem() {
        Material mat = MechanicMapper.getMechMaterial(this.getClass());
        ItemStack mechanicStack = new ItemStack(mat);

        List<Component> explanationLore;
        if(item!= null) explanationLore = List.of(Component.text("Current Item: " + item.getType().name()));
        else explanationLore = List.of(Component.text("Current Item: None"));

        ItemMeta mechanicMeta = mechanicStack.getItemMeta();
        Mechanic.embedUUID(mechanicMeta, mechanicID);
        mechanicMeta.displayName(Component.text(MechanicMapper.getMechName(this.getClass())).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        mechanicMeta.lore(explanationLore);
        mechanicStack.setItemMeta(mechanicMeta);

        return mechanicStack;
    }


    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {
        Inventory mechanicInv = Bukkit.createInventory(player, 27, Component.text("Mechanic: " + MechanicMapper.getMechName(this.getClass()))
                .color(NamedTextColor.BLUE));

        mechanicInv.setItem(4, createIDItem(mechanicID, Material.BOOK));

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

        ItemStack back = new ItemStack(Material.RED_CONCRETE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);
        mechanicInv.setItem(24, back);

        ItemStack delete = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.displayName(Component.text("Delete Mechanic").color(NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
        delete.setItemMeta(deleteMeta);
        mechanicInv.setItem(26, delete);

        return mechanicInv;
    }

    public void setItem(ItemStack item) {
        if(item != null) this.item = item.clone();
    }

    public ItemStack getItem() {
        if(item != null) {
            return item.clone();
        }
        return null;

    }

    @Override
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {

        Player p = (Player) e.getInventory().getHolder();

        if (e.getClickedInventory() != null && e.getClickedInventory() != p.getInventory()) {
            e.setCancelled(true);
            String itemName = PlainTextComponentSerializer.plainText().serialize(e.getCurrentItem().displayName());

            if(itemName.contains("Place Item Here!")) {
                ItemStack heldItem = e.getCursor();

                if(heldItem.getType() != Material.AIR) {
                    setItem(heldItem);
                    //should probably move refresh elsewhere, but this does the refresh
                    p.openInventory(generateMechanicSettingsMenu(p));
                    getLevel().writeData();
                }

            } else if (e.getSlot() == 15) {
                if(getItem() != null) {
                    p.getInventory().addItem(getItem());
                }
            }
        }
    }
}
