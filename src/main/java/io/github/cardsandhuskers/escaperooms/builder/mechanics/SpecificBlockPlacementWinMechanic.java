package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpecificBlockPlacementWinMechanic extends Mechanic {


    public SpecificBlockPlacementWinMechanic(Level level) {
        super();
        this.level = level;
    }

    public SpecificBlockPlacementWinMechanic(String mechanicID, Level level, ConfigurationSection attributes) {
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

    @Override
    public @NotNull Map<String, Object> serialize() {
        return null;
    }
}
