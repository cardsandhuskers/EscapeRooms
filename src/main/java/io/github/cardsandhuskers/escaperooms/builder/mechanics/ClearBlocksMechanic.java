package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class ClearBlocksMechanic extends Mechanic{
    @Override
    public Map<String, Object> serialize() {
        return null;
    }

    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {
        return null;
    }

    @Override
    public List<Component> getLore() {
        return null;
    }

    @Override
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {

    }

    @Override
    public void eventHandler(TeamInstance teamInstance, Event e) {

    }

    @Override
    public void levelStartExecution(TeamInstance teamInstance) {

    }
}
