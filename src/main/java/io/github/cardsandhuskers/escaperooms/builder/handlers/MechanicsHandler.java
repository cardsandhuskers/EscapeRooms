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
     * switch matches Level.addMechanic()
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

    public void onDeleteMechanicClick(InventoryClickEvent e, ItemStack clickedItem) {
        Player p = (Player)e.getInventory().getHolder();

        ItemStack titleItem = e.getInventory().getItem(4);
        UUID id = Mechanic.getUUIDFromItem(titleItem);
        Mechanic mech = findMechanicFromID(id);

        if(clickedItem.getType() == Material.RED_CONCRETE) {
             p.openInventory(mech.generateMechanicSettingsMenu(p));
        } else if (clickedItem.getType() == Material.LIME_CONCRETE) {
            Level level = mech.getLevel(); //TODO should open level inv but can't because this code is ass
            mech.delete();

            p.closeInventory();
        }
    }

    public Mechanic findMechanicFromID(UUID id) {
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
