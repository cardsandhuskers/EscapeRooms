package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.EditorGUI;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Disables interaction with a certain block type
 */
public class DisableInteractMechanic extends Mechanic {
    private final List<Material> disabledBlocks = new ArrayList<>();


    public DisableInteractMechanic(Level level) {
        super();
        this.level = level;
    }

    public DisableInteractMechanic(String mechanicID, Level level, ConfigurationSection attributes) {

        this.mechanicID = UUID.fromString(mechanicID);
        this.level = level;


        List<String> materials = (List<String>) attributes.getList("materials");
        if (materials != null) {
            for (String mat : materials) {
                // Deserialize each location into a BlockLocation
                disabledBlocks.add(Material.getMaterial(mat));
            }
        }
    }


    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {

        Inventory mechanicInv = Bukkit.createInventory(player, 36, Component.text("Mechanic: " + MechanicMapper.getMechName(this.getClass()))
                .color(NamedTextColor.BLUE));

        //ID item
        mechanicInv.setItem(4, createIDItem(mechanicID, Material.FURNACE));

        Component explainerName = Component.text(">>>").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE);
        List<Component> explanationLore = List.of(
                Component.text("Place an item in the orange"),
                Component.text("concrete to add it to the"),
                Component.text("list of blocks to disable"),
                Component.text("interacting with."));
        mechanicInv.setItem(0, createItem(Material.ARROW, 1, explainerName, explanationLore));

        Component placementName = Component.text("Place Item Here!").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.GOLD);
        mechanicInv.setItem(1, createItem(Material.ORANGE_CONCRETE, 1, placementName, null));

        int i = 9;
        for(Material mat: disabledBlocks) {
            mechanicInv.setItem(i, createItem(mat, 1, null, null));
            i++;
        }

        mechanicInv.setItem(27, EditorGUI.createBackButton());
        mechanicInv.setItem(35, EditorGUI.createDeleteButton());

        return mechanicInv;

    }

    @Override
    public List<Component> getLore() {
        return null;
    }

    @Override
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {

        if(e.getClickedInventory() != e.getWhoClicked().getInventory()) {
            e.setCancelled(true);

            //title item, just ignore
            if(e.getSlot() == 4) return;

            ItemStack item = e.getCurrentItem();
            Material mat = item.getType();


            if(mat == Material.ORANGE_CONCRETE) {
                ItemStack heldItem = e.getCursor();
                Material heldMat = heldItem.getType();

                if(!disabledBlocks.contains(heldMat)) {
                    disabledBlocks.add(heldMat);
                } else {
                    e.getWhoClicked().sendMessage("Already Exists");
                }
            }

            if(e.getClick() == ClickType.RIGHT) {
                disabledBlocks.remove(mat);
            }
            e.getWhoClicked().openInventory(generateMechanicSettingsMenu((Player) e.getWhoClicked()));
            getLevel().writeData();
        }
    }

    @Override
    public void eventHandler(TeamInstance teamInstance, Event e) {
        if(e instanceof PlayerInteractEvent pie) {
            if(pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block b = pie.getClickedBlock();
                if(b != null) {
                    Material mat = b.getType();

                    if(disabledBlocks.contains(mat)) {
                        pie.setCancelled(true);
                    }
                }
            }
        }
    }

    @Override
    public void levelStartExecution(TeamInstance teamInstance) {
        //nothing to do here
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        HashMap<String, Object> attributes = new HashMap<>();

        List<String> materialStrings = new ArrayList<>();
        for(Material mat: disabledBlocks) {
            materialStrings.add(mat.name());
        }

        attributes.put("materials", materialStrings);
        attributes.put("type", MechanicMapper.getMechName(this.getClass()));
        return null;
    }
}
