package io.github.cardsandhuskers.escaperooms.builder.listeners;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.buttonmechanic.RandomButtonMechanic;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listens for player interacting with the world using the blaze rod to select locations for the random button mechanic
 * Unregisters itself 30 seconds after being registered
 */
public class ButtonMechanicClickListener implements Listener, Runnable {
    private final RandomButtonMechanic mechanic;
    private Integer assignedTaskId;
    private final Player player;
    private final int CANCEL_TIME = 120;
    EditorGUIHandler editorGUIHandler;

    public ButtonMechanicClickListener(RandomButtonMechanic mechanic, Player player, EditorGUIHandler editorGUIHandler) {
        this.mechanic = mechanic;
        this.player = player;
        this.editorGUIHandler = editorGUIHandler;
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        if (e.getPlayer() != player) return;

        ItemStack heldItem = e.getItem();
        Action action = e.getAction();

        if (heldItem != null && heldItem.getType() == Material.BLAZE_ROD) {
            System.out.println("TEST B");
            e.setCancelled(true);
            if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
                System.out.println("TEST C");
                cancelOperation();
                HandlerList.unregisterAll(this);
                editorGUIHandler.getPlayerMenu(player).openEditMechanicInv(mechanic);


            } else if (action == Action.RIGHT_CLICK_BLOCK) {
                System.out.println("TEST D");
                BlockFace face = e.getBlockFace();
                Block block = e.getClickedBlock();

                System.out.println(block);
                System.out.println(face);

                boolean success = mechanic.addLocation(block, face);
                if(success) {
                    player.sendMessage(Component.text("Added button location on the " + face + " face of the " + block.getType() + " block at: " +
                            "\nX: " + block.getX() +
                            "\nY: " + block.getY() +
                            "\nZ: " + block.getZ()));

                    mechanic.getLevel().writeData();
                } else {
                    player.sendMessage("Please set both pos1 and pos2 corners before adding location based mechanics.");
                    cancelOperation();
                    HandlerList.unregisterAll(this);
                }
            }
        }
    }

    /**
     * Unregisters the listener, called 30 seconds after the listener is registered
     */
    @Override
    public void run() {
        player.sendMessage("30 Seconds has Elapsed, cancelling button set listener.");
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
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(EscapeRooms.getPlugin(), this, CANCEL_TIME * 20, 1L);
    }
}
