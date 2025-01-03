package io.github.cardsandhuskers.escaperooms.builder.listeners;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {
    EditorGUIHandler editorGUIHandler;

    public InventoryCloseListener(EditorGUIHandler editorGUIHandler) {
        this.editorGUIHandler = editorGUIHandler;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        String title = PlainTextComponentSerializer.plainText().serialize(e.getView().title());
        if(title.equals("Escape Room GUI")|| title.contains("Editor")) {
            editorGUIHandler.onGUIExit((Player) e.getPlayer());
        }


    }
}
