package io.github.cardsandhuskers.escaperooms.builder.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.StartingItemMechanic;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {
        EscapeRooms plugin = EscapeRooms.getInstance();
        Player p = (Player) e.getInventory().getHolder();
        ItemStack titleItem = e.getInventory().getItem(4);
        ItemMeta titleMeta = titleItem.getItemMeta();
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        PersistentDataContainer container = titleMeta.getPersistentDataContainer();
        UUID id = UUID.fromString(container.get(namespacedKey, PersistentDataType.STRING));
        Mechanic match = findMechanicFromID(id);

        if(e.getCurrentItem().getType() == Material.RED_CONCRETE) {
            editorGUIHandler.getPlayerMenu(p).openEditInv(match.getLevel().getName());
        }

        if(match instanceof StartingItemMechanic startingItemMech) {
            //do stuff for this mechanic here
            //text: Place Item Here!
            if (e.getClickedInventory() != null && e.getClickedInventory() != p.getInventory()) {
                if(PlainTextComponentSerializer.plainText().serialize(e.getCurrentItem().displayName()).contains("Place Item Here!")) {
                    ItemStack heldItem = e.getCursor();
                    System.out.println("HELD ITEM: " + heldItem.getType());
                    startingItemMech.setItem(heldItem);
                    //should probably move refresh
                    p.openInventory(match.generateMechanicSettingsMenu(p));

                }

                e.setCancelled(true);
                startingItemMech.getLevel().writeData();
            }


        }

    }

    public Mechanic findMechanicFromID(UUID id) {
        for (Level l:LevelHandler.getInstance().getLevels()) {
            for(Mechanic m: l.getMechanics()) {
                if(m.getID().equals(id)) {
                    return m;
                }
            }
        }
        return null;
    }

}
