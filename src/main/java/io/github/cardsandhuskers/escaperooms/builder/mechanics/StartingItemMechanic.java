package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.EditorGUI;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Mechanic to give an item to all players on level start
 */
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
    public Inventory generateMechanicSettingsMenu(Player player) {
        Inventory mechanicInv = Bukkit.createInventory(player, 27, Component.text("Mechanic: " + MechanicMapper.getMechName(this.getClass()))
                .color(NamedTextColor.BLUE));

        mechanicInv.setItem(4, createIDItem(mechanicID, Material.BOOK));

        Component explainerName = Component.text(">>>").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        List<Component> explanationLore = List.of(
                Component.text("Place an item in the orange"),
                Component.text("concrete to select it as the"),
                Component.text("item to give to players."));
        mechanicInv.setItem(11, createItem(Material.ARROW, 1, explainerName, explanationLore));

        Component placementName = Component.text("Place Item Here!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD);
        mechanicInv.setItem(12, createItem(Material.ORANGE_CONCRETE, 1, placementName, null));

        Component currExplainerName = Component.text("Current Item:").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        List<Component> currExplainerLore = List.of(
                Component.text("The item on the right is what"),
                Component.text("is currently stored to give "),
                Component.text("to players."));
        mechanicInv.setItem(14, createItem(Material.ARROW, 1, currExplainerName, currExplainerLore));

        if(getItem() == null) {
            Component holderName = Component.text("None!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
            mechanicInv.setItem(15, createItem(Material.GLASS_PANE, 1, holderName, null));
        } else {
            mechanicInv.setItem(15, getItem());
        }

        mechanicInv.setItem(18, EditorGUI.createBackButton());
        mechanicInv.setItem(26, EditorGUI.createDeleteButton());
        return mechanicInv;
    }

    @Override
    public List<Component> getLore() {
        List<Component> explanationLore;
        if(item!= null) explanationLore = List.of(Component.text("Current Item: " + item.getType().name()));
        else explanationLore = List.of(Component.text("Current Item: None"));
        return explanationLore;
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

    @Override
    public void eventHandler(TeamInstance teamInstance, Event e) {
        if (e instanceof PlayerJoinEvent pje) {
            pje.getPlayer().getInventory().addItem(item);
        }
    }

    @Override
    public void levelStartExecution(TeamInstance teamInstance) {
        if(item == null) return;

        for(Player p: teamInstance.getTeam().getOnlinePlayers()) {
            p.getInventory().addItem(item);
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {

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

    public void setItem(ItemStack item) {
        if(item != null) this.item = item.clone();
    }

    public ItemStack getItem() {
        if(item != null) {
            return item.clone();
        }
        return null;

    }
}
