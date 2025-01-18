package io.github.cardsandhuskers.escaperooms.builder.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.listeners.CornerWandClickListener;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.objects.EditorGUI;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.UUID;

public class EditorGUIHandler {
    private HashMap<OfflinePlayer, EditorGUI> guiMap = new HashMap<>();
    private EscapeRooms plugin = EscapeRooms.getPlugin();

    public EditorGUIHandler() {
    }

    public void onMainGUIOpen(Player p) {
        getPlayerMenu(p).openMainInv();
    }

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
                p.getInventory().addItem(new ItemStack(mat));

                CornerWandClickListener cornerWandClickListener = new CornerWandClickListener(this, currLevel, mat, p);
                cornerWandClickListener.startOperation();
                plugin.getServer().getPluginManager().registerEvents(cornerWandClickListener, plugin);
                p.closeInventory();
            }
            case BARRIER -> {
                gui.openDeleteLevelMenu(currLevel);

            }
            case NETHER_STAR -> gui.openAddMechanicInv();
            case ENDER_PEARL -> currLevel.setSpawnPoint(p.getLocation());
            case GREEN_CONCRETE -> currLevel.saveSchematic();
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

        if(MechanicMapper.isValidMaterial(mat)) {

            ItemMeta itemMeta = clickedItem.getItemMeta();
            NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            String idString = container.get(namespacedKey, PersistentDataType.STRING);
            if(idString != null) {
                UUID id = UUID.fromString(idString);

                Mechanic mechanic = MechanicsHandler.getInstance().findMechanicFromID(id);
                gui.openEditMechanicInv(mechanic);
            } else {
                //do nothing tbh
            }
        }

        currLevel.writeData();
        refreshAll();

    }

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

    public void onGUIExit(Player p) {
        EditorGUI gui = guiMap.get((OfflinePlayer) p);

        if(gui != null) {
            gui.close();
        }
    }

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
