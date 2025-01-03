package io.github.cardsandhuskers.escaperooms.builder.listeners;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for level position set wands.
 * Should use some countdown timer to unregister this if the player does not use their wand within set time
 */
public class PlayerClickListener implements Listener {
    private EditorGUIHandler editorGUIHandler;
    private Level currLevel;
    private Material mat;

    public PlayerClickListener(EditorGUIHandler editorGUIHandler, Level currLevel, Material mat) {
        this.editorGUIHandler = editorGUIHandler;
        this.currLevel = currLevel;
        this.mat = mat;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getClickedBlock().getType() != Material.AIR) {
            //do stuff
            ItemStack heldItem = e.getItem();
            if (heldItem != null) {
                if (heldItem.getType() == mat){
                    LevelHandler.getInstance().setLevelPos(e.getClickedBlock().getLocation(), currLevel, mat);
                    e.getPlayer().getInventory().setItemInMainHand(null);

                    editorGUIHandler.getPlayerMenu(e.getPlayer()).openEditInv(currLevel.getName());
                    editorGUIHandler.refreshAll();
                    HandlerList.unregisterAll(this);
                }
            }


        }
    }


}
