package io.github.cardsandhuskers.escaperooms.builder.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.listeners.CornerWandClickListener;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.objects.EditorGUI;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EditorGUIHandler {
    private HashMap<OfflinePlayer, EditorGUI> guiMap = new HashMap<>();
    private EscapeRooms plugin = EscapeRooms.getPlugin();

    public EditorGUIHandler() {
    }

    public void onMainGUIOpen(Player p) {
        getPlayerMenu(p).openMainInv();
    }

    /**
     * Click handler for main GUI (one with list of levels and ability to add levels)
     * @param p
     * @param clickedItem
     * @param clickType
     */
    public void onMainGUIClick(Player p, ItemStack clickedItem, ClickType clickType) {
        //switch function for different items
        Component display = clickedItem.getItemMeta().displayName();
        if(display == null) return;

        String itemName = PlainTextComponentSerializer.plainText().serialize(display);
        Material mat = clickedItem.getType();
        EditorGUI gui = guiMap.get(p);


        switch (mat) {
            case NAME_TAG:
                //make level
                gui.makeNewLevel(this);
                break;
            case FLOWER_BANNER_PATTERN:
                //open gui for level
                gui.openEditInv(itemName);
                break;
        }

    }

    /**
     * Handler for a click on the level editor GUI
     * @param p
     * @param clickedItem
     * @param clickType
     * @param levelName
     */
    public void onEditorGUIClick(Player p, ItemStack clickedItem, ClickType clickType, String levelName) {
        Component displayName = clickedItem.getItemMeta().displayName();
        if(displayName == null) return;
        String itemName = PlainTextComponentSerializer.plainText().serialize(displayName);

        Level currLevel = LevelHandler.getInstance().getLevel(levelName);
        Material mat = clickedItem.getType();
        EditorGUI gui = guiMap.get(p);

        if(currLevel == null) {
            plugin.getLogger().warning("Level selected is null!");
            return;
        }

        switch (mat) {
            case BLAZE_ROD, BREEZE_ROD -> {
                ItemStack wand = new ItemStack(mat);
                ItemMeta wandMeta = wand.getItemMeta();
                wandMeta.displayName(Component.text("Set Level Corner").decoration(TextDecoration.ITALIC, false));
                wandMeta.lore(List.of(Component.text("Sets the level's corner wherever you click"), Component.text("Expires in 30 seconds if you do not click anywhere.")));
                wand.setItemMeta(wandMeta);

                p.getInventory().addItem(wand);

                CornerWandClickListener cornerWandClickListener = new CornerWandClickListener(this, currLevel, mat, p);
                cornerWandClickListener.startOperation();
                plugin.getServer().getPluginManager().registerEvents(cornerWandClickListener, plugin);
                p.closeInventory();
            }
            case BARRIER -> {
                gui.openDeleteLevelMenu(currLevel);

            }
            case NETHER_STAR -> gui.openAddMechanicInv();
            case ENDER_PEARL -> {
                boolean result = currLevel.setSpawnPoint(p.getLocation());
                if(!result) p.sendMessage(Component.text("Level positions are not set.").color(NamedTextColor.RED));
            }
            case OAK_BUTTON -> {
                boolean result = currLevel.setLevelEndButton(p.getLocation());
                if(!result) p.sendMessage(Component.text("Either Level positions are not set or block at your head is not a button.").color(NamedTextColor.RED));
            }
            case GREEN_CONCRETE -> {
                boolean result = currLevel.saveSchematic();
                if(result) p.sendMessage(Component.text("Successfully Saved Level Schematic").color(NamedTextColor.GREEN));
                else p.sendMessage(Component.text("Error Saving Level Schematic").color(NamedTextColor.RED));
            }
        }
        switch(itemName) {
            case "Back"-> gui.openMainInv();
            case "Toggle Environment Damage" -> {
                if(currLevel.isEnvDamage()) currLevel.setEnvDamage(false);
                else currLevel.setEnvDamage(true);
            }
            case "Toggle PVP Damage" -> {
                if(currLevel.isPvpDamage()) currLevel.setPvpDamage(false);
                else currLevel.setPvpDamage(true);
            }
            case "Change Level GameMode" -> {
                switch (currLevel.getGameMode()) {
                    case SURVIVAL -> currLevel.setGameMode(GameMode.ADVENTURE);
                    case ADVENTURE -> currLevel.setGameMode(GameMode.CREATIVE);
                    case CREATIVE -> currLevel.setGameMode(GameMode.SURVIVAL);
                }
            }
            case "Change Minimum Players" -> {
                switch (currLevel.getMinPlayers()) {
                    case 1 -> currLevel.setMinPlayers(2);
                    case 2 -> currLevel.setMinPlayers(3);
                    case 3 -> currLevel.setMinPlayers(4);
                    case 4 -> currLevel.setMinPlayers(1);
                }
            }
        }

        // plugin.getComponentLogger().debug(Component.text("idk"));

        if(MechanicMapper.isValidMaterial(mat)) {
            UUID id = Mechanic.getUUIDFromItem(clickedItem);
            System.out.println("id: " + id);
            if (id != null) {
                Mechanic mechanic = MechanicsHandler.getInstance().findMechanicFromID(id);
                gui.openEditMechanicInv(mechanic);
            }
        }

        currLevel.writeData();
        refreshAll();

    }

    /**
     * Handler for a click within the addMechanic GUI
     * @param p
     * @param clickedItem
     */
    public void onMechanicGUIClick(Player p, ItemStack clickedItem) {
        EditorGUI gui = getPlayerMenu(p);
        Level currLevel = gui.getCurrentSelectedLevel();

        if(clickedItem.getType() == Material.RED_CONCRETE) {
            gui.openEditInv(currLevel.getName());
        } else {
            Mechanic mechanic = currLevel.addMechanic(clickedItem.getType());
            gui.openEditMechanicInv(mechanic);
        }

    }

    /**
     * Click handler for a click within the confirmation menu for deleting a level
     * @param e
     * @param clickedItem
     */
    public void onDeleteLevelClick(InventoryClickEvent e, ItemStack clickedItem) {

        Component displayName = e.getClickedInventory().getItem(4).getItemMeta().displayName();
        if(displayName == null) return;
        String itemName = PlainTextComponentSerializer.plainText().serialize(displayName);
        System.out.println(itemName);

        if(clickedItem.getType() == Material.RED_CONCRETE) {
            getPlayerMenu((Player) e.getInventory().getHolder()).openEditInv(itemName);

        } else if (clickedItem.getType() == Material.LIME_CONCRETE) {
            LevelHandler.getInstance().deleteLevel(itemName);
            onMainGUIOpen((Player) e.getInventory().getHolder());
        }
    }

    /**
     * called when GUI closes, triggers the close operation in the GUI object
     * @param p
     */
    public void onGUIExit(Player p) {
        EditorGUI gui = guiMap.get((OfflinePlayer) p);

        if(gui != null) {
            gui.close();
        }
    }

    /**
     * Returns a GUI for a specific player
     * @param p
     * @return
     */
    public EditorGUI getPlayerMenu(Player p) {
        EditorGUI playerGUI = guiMap.get((OfflinePlayer) p);
        if(playerGUI != null) {
            return playerGUI;
        } else {
            playerGUI = new EditorGUI(p);
            guiMap.put((OfflinePlayer) p, playerGUI);
            return playerGUI;
        }
    }

    /**
     * Refreshes all menus
     */
    public void refreshAll() {
        for (EditorGUI gui: guiMap.values()) {
            if (gui.isOpen()) {
                EditorGUI.GUI type = gui.getCurrentGUI();
                switch (type) {
                    case MAIN:
                        gui.openMainInv();
                        break;
                    case LEVEL_EDITOR:
                        Level currentLevel = gui.getCurrentSelectedLevel();
                        if(currentLevel != null) {
                            gui.openEditInv(currentLevel.getName());
                        } else {
                            plugin.getLogger().warning("Level is null!");
                        }
                        break;
                    case MECHANIC_SETTINGS:
                        //TODO: data needed for this is not here
                }
            }
        }
    }

}
