package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ClearBlocksMechanic extends Mechanic{
    @Override
    public Map<String, Object> getData() {
        return null;
    }

    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {
        return null;
    }

    @Override
    public ItemStack createItem() {
        return null;
    }

    @Override
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {

    }
}
