package io.github.cardsandhuskers.escaperooms.builder.listeners;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for level position set wands.
 * Uses a countdown timer to cancel the listener after 30 seconds
 */
public class CornerWandClickListener implements Listener, Runnable {
    private EditorGUIHandler editorGUIHandler;
    private Level currLevel;
    private Material mat;
    // Our scheduled task's assigned id, needed for canceling
    private Integer assignedTaskId;
    private Player player;
    private final int cancelTime = 30;

    public CornerWandClickListener(EditorGUIHandler editorGUIHandler, Level currLevel, Material mat, Player player) {
        this.editorGUIHandler = editorGUIHandler;
        this.currLevel = currLevel;
        this.mat = mat;
        this.player = player;

    }

    /**
     *
     * @param e
     */
    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        if(e.getPlayer() != player) return;

        if (e.getClickedBlock() != null && e.getClickedBlock().getType() != Material.AIR) {
            //do stuff
            ItemStack heldItem = e.getItem();
            if (heldItem != null && heldItem.getType() == mat) {
                e.setCancelled(true);
                LevelHandler.getInstance().setLevelPos(e.getClickedBlock().getLocation(), currLevel, mat);
                e.getPlayer().getInventory().setItemInMainHand(null);

                editorGUIHandler.getPlayerMenu(e.getPlayer()).openEditInv(currLevel.getName());
                editorGUIHandler.refreshAll();
                HandlerList.unregisterAll(this);
                cancelOperation();
            }
        }
    }

    /**
     * Unregisters the listener, called 30 seconds after the listener is registered
     */
    @Override
    public void run() {
        player.sendMessage("30 Seconds has Elapsed, cancelling corner listener.");
        HandlerList.unregisterAll(this);
        cancelOperation();
    }

    /**
     * Stop the repeating task
     */
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    }


    /**
     * Schedules this instance to "run" every tick
     */
    public void startOperation() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(EscapeRooms.getPlugin(), this, cancelTime * 20, 1L);
    }


}
