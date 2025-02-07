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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mechanic that supports giving custom drops when breaking a certain block type
 */
public class CustomDropMechanic extends Mechanic{

    private Material baseBlock = null;
    private ItemStack customDrop;

    public CustomDropMechanic(Level level) {
        super();
        this.level = level;
    }

    public CustomDropMechanic(String mechanicID, Level level, ConfigurationSection attributes) {
        this.mechanicID = UUID.fromString(mechanicID);
        this.level = level;

        String itemString = attributes.getString("customDrop");
        if(itemString != null && !itemString.isEmpty()) {
            customDrop = Mechanic.deserializeItemStack(itemString);
        }

        String baseMaterial = attributes.getString("baseBlock");
        if(baseMaterial != null && !baseMaterial.isEmpty()) {
            baseBlock = Material.valueOf(baseMaterial);
        }

    }

    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {
        Inventory mechanicInv = Bukkit.createInventory(player, 36, Component.text("Mechanic: " + MechanicMapper.getMechName(this.getClass()))
                .color(NamedTextColor.BLUE));

        mechanicInv.setItem(4, createIDItem(mechanicID, Material.DIAMOND_PICKAXE));

        Component explainerName = Component.text(">>>").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        List<Component> explainerLore = List.of(
                Component.text("Place an item in the orange concrete"),
                Component.text("to select it as the block to alter"),
                Component.text("the drop for when it is broken."));
        mechanicInv.setItem(11, createItem(Material.ARROW, 1, explainerName, explainerLore));

        Component alterPlacementName = Component.text("Place Item to Alter Here!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD);
        mechanicInv.setItem(12, createItem(Material.ORANGE_CONCRETE, 1, alterPlacementName, null));

        Component dropExplainerName = Component.text(">>>").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        List<Component> dropExplainerLore = List.of(
                Component.text("Place an item in the yellow concrete"),
                Component.text("to select it as the item to give when"),
                Component.text("the altered block is broken."));
        mechanicInv.setItem(14, createItem(Material.ARROW, 1, dropExplainerName, dropExplainerLore));

        Component dropPlacementName = Component.text("Place Item to Give on Drop Here!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD);
        mechanicInv.setItem(15, createItem(Material.ORANGE_CONCRETE, 1, dropPlacementName, null));


        Component currAlterExplainerName = Component.text("Current Item:").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        List<Component> currAlterExplainerLore = List.of(
                Component.text("The item on the right is what"),
                Component.text("is currently stored as the block "),
                Component.text("to apply the custom drop to."));
        mechanicInv.setItem(20, createItem(Material.ARROW, 1, currAlterExplainerName, currAlterExplainerLore));

        Component currDropExplainerName = Component.text("Current Item:").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        List<Component> currDropExplainerLore = List.of(
                Component.text("The item on the right is what is"),
                Component.text("currently stored to drop when"),
                Component.text("the altered block is broken."));
        mechanicInv.setItem(23, createItem(Material.ARROW, 1, currDropExplainerName, currDropExplainerLore));

        if(baseBlock == null) {
            Component holderName = Component.text("None!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
            mechanicInv.setItem(21, createItem(Material.GLASS_PANE, 1, holderName, null));
        } else {
            mechanicInv.setItem(21, createItem(baseBlock, 1, null, null));
        }

        if(customDrop == null) {
            Component holderName = Component.text("None!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
            mechanicInv.setItem(24, createItem(Material.GLASS_PANE, 1, holderName, null));
        } else {
            mechanicInv.setItem(24, customDrop);
        }

        mechanicInv.setItem(27, EditorGUI.createBackButton());
        mechanicInv.setItem(35, EditorGUI.createDeleteButton());

        return mechanicInv;
    }

    @Override
    public List<Component> getLore() {
        List<Component> explanationLore = List.of(
                Component.text("Breaking " + baseBlock.name()),
                Component.text("Drops " + customDrop.displayName())
        );
        return explanationLore;
    }

    @Override
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {
        Player p = (Player) e.getInventory().getHolder();

        if (e.getClickedInventory() != null && e.getClickedInventory() != p.getInventory()) {
            e.setCancelled(true);
            String itemName = PlainTextComponentSerializer.plainText().serialize(e.getCurrentItem().displayName());

            if(itemName.contains("Place Item to Alter Here")) {
                ItemStack heldItem = e.getCursor();
                if(heldItem.getType() != Material.AIR) {
                    baseBlock = heldItem.getType();
                    p.openInventory(generateMechanicSettingsMenu(p));
                    getLevel().writeData();
                }
            }
            else if (itemName.contains("Place Item to Give on Drop Here!")) {
                ItemStack heldItem = e.getCursor();
                if(heldItem.getType() != Material.AIR) {
                    customDrop = heldItem.clone();
                    p.openInventory(generateMechanicSettingsMenu(p));
                    getLevel().writeData();
                }
            } else if (e.getSlot() == 24) {
                if(customDrop != null) {
                    p.getInventory().addItem(customDrop.clone());
                }
            }

        }

    }

    @Override
    public void eventHandler(TeamInstance teamInstance, Event e) {
        if(e instanceof BlockBreakEvent bee) {
            if(bee.getBlock().getType() == baseBlock) {
                bee.setCancelled(true);
                bee.getBlock().setType(Material.AIR);

                bee.getBlock().getWorld().dropItemNaturally(bee.getBlock().getLocation(), customDrop);

            }
        }
    }

    @Override
    public void levelStartExecution(TeamInstance teamInstance) {
        //nothing to do here...
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> attributes = new HashMap<>();
        if (baseBlock != null) {
            // Serialize the ItemStack
            attributes.put("baseBlock", baseBlock.name());
        }
        if(customDrop != null) {
            attributes.put("customDrop", serializeItemStack(customDrop));
        }

        // Set other attributes like "type"
        attributes.put("type", MechanicMapper.getMechName(this.getClass()));

        return attributes;
    }
}
