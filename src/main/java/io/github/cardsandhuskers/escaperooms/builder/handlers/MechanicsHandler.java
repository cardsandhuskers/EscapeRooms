package io.github.cardsandhuskers.escaperooms.builder.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MechanicsHandler {
    private static MechanicsHandler mechanicsHandler;

    private HashMap<String, Material> mechanicTypes = new HashMap<>();
    private MechanicsHandler() {

    }

    public static MechanicsHandler getInstance() {
        if(mechanicsHandler == null) {
            mechanicsHandler = new MechanicsHandler();
        }
        return mechanicsHandler;
    }

    /**
     * gets the mechanic clicked using the item in the inventory title slot (slot 4)
     * @param e
     */
    public void onMechanicClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {
        Player p = (Player) e.getInventory().getHolder();
        ItemStack titleItem = e.getInventory().getItem(4);
        UUID id = Mechanic.getUUIDFromItem(titleItem);
        Mechanic mech = findMechanicFromID(id);

        if(e.getCurrentItem().getType() == Material.RED_CONCRETE) {
            editorGUIHandler.getPlayerMenu(p).openEditInv(mech.getLevel().getName());
        } else if (e.getCurrentItem().getType() == Material.BARRIER) {
            mech.openDeleteMenu(p);
        }
        else {
            mech.handleClick(e, editorGUIHandler);
        }

    }

    /**
     * Handles a click within the GUI to confirm deletion of a
     * @param e - click event
     * @param clickedItem - clicked item
     * @param editorGUIHandler - GUI handler so that the level editor can be returned to
     */
    public void onDeleteMechanicClick(InventoryClickEvent e, ItemStack clickedItem, EditorGUIHandler editorGUIHandler) {
        Player p = (Player)e.getInventory().getHolder();

        ItemStack titleItem = e.getInventory().getItem(4);
        UUID id = Mechanic.getUUIDFromItem(titleItem);
        Mechanic mech = findMechanicFromID(id);

        if(clickedItem.getType() == Material.RED_CONCRETE) {
             p.openInventory(mech.generateMechanicSettingsMenu(p));
        } else if (clickedItem.getType() == Material.LIME_CONCRETE) {
            Level level = mech.getLevel();
            editorGUIHandler.getPlayerMenu(p).openEditInv(level.getName());
            mech.delete();

            p.closeInventory();
        }
    }

    /**
     * Returns mechanic object from it's UUID
     * @param id - mechanic's UUID
     * @return - Mechanic object
     */
    public Mechanic findMechanicFromID(UUID id) {
        if(id == null)
            return null;
        for (Level l : LevelHandler.getInstance().getLevels()) {
            for (Mechanic m : l.getMechanics()) {
                if (m.getID().equals(id)) {
                    return m;
                }
            }
        }
        return null;
    }

}
