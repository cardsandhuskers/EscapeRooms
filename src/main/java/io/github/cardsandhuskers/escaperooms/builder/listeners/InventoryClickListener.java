package io.github.cardsandhuskers.escaperooms.builder.listeners;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.handlers.MechanicsHandler;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Main inventory click listener for the GUIs, calls appropriate functions based on which GUI the user clicks in
 */
public class InventoryClickListener implements Listener {
    private EditorGUIHandler editorGUIHandler;

    public InventoryClickListener(EditorGUIHandler editorGUIHandler) {
        this.editorGUIHandler = editorGUIHandler;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        ItemStack clickedItem = e.getCurrentItem();
        try {
            Player p = (Player) e.getInventory().getHolder();
        } catch (Exception ex) {
            return;
        }
        ClickType clickType = e.getClick();

        if(clickedItem != null) {
            if(title.equals("Escape Room GUI")) {
                e.setCancelled(true);
                editorGUIHandler.onMainGUIClick((Player) e.getInventory().getHolder(), clickedItem, clickType);
            } else if (title.contains("Editor")) {
                e.setCancelled(true);
                String rawName = PlainTextComponentSerializer.plainText().serialize(e.getInventory().getItem(4).displayName());
                // Remove brackets manually if present
                String levelName = rawName.replaceAll("\\[|\\]", ""); // Removes "[" and "]"
                editorGUIHandler.onEditorGUIClick((Player) e.getInventory().getHolder(), clickedItem, clickType, levelName);
            } else if (title.equals("Select a Mechanic")) {
                e.setCancelled(true);

                editorGUIHandler.onMechanicGUIClick((Player) e.getInventory().getHolder(), clickedItem);
            } else if(title.contains("Mechanic:")) {
                //e.setCancelled(true);
                MechanicsHandler.getInstance().onMechanicClick(e, editorGUIHandler);
            } else if (title.contains("Delete Level")) {
                e.setCancelled(true);
                editorGUIHandler.onDeleteLevelClick(e, clickedItem);
            } else if (title.contains("Delete Mechanic")) {
                e.setCancelled(true);
                MechanicsHandler.getInstance().onDeleteMechanicClick(e, clickedItem, editorGUIHandler);
            }
        }

    }
}
