package io.github.cardsandhuskers.escaperooms.builder.objects;
import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO: ADD second class for other menu, combining them is not good
 */
public class EditorGUI {
    private Inventory mainInv;
    private Inventory editInv;
    private Player player;
    private EscapeRooms plugin = EscapeRooms.getPlugin();
    private GUI currentGUI;

    private Level currentSelectedLevel;
    private boolean isOpen = false;

    public EditorGUI(Player p) {
        player = p;

        this.plugin = plugin;
    }

    public Level getCurrentSelectedLevel() {
        return currentSelectedLevel;
    }

    public void generateMainMenu() {
        mainInv = Bukkit.createInventory(player, 54, Component.text("Escape Room GUI")
                .color(NamedTextColor.GREEN));

        ItemStack newLevelTag = new ItemStack(Material.NAME_TAG);
        ItemMeta tagMeta = newLevelTag.getItemMeta();
        tagMeta.displayName(Component.text("Create Level"));
        newLevelTag.setItemMeta(tagMeta);
        fillSpacers(mainInv);

        mainInv.setItem(49, newLevelTag);

        //level items
        int i = 9;
        ItemStack levelItem = new ItemStack(Material.FLOWER_BANNER_PATTERN);
        ItemMeta levelItemMeta = levelItem.getItemMeta();
        for (Level l: LevelHandler.getInstance().getLevels()) {
            levelItemMeta.displayName(Component.text(l.getName()));
            levelItem.setItemMeta(levelItemMeta);
            mainInv.setItem(i, levelItem);
            i++;
        }
    }

    public void generateLevelEditMenu(String itemName) {
        editInv = Bukkit.createInventory(player, 54, Component.text("Level " + itemName + " Editor")
                .color(NamedTextColor.GREEN));

        Level level = LevelHandler.getInstance().getLevel(itemName);
        if (level == null) {
            plugin.getLogger().severe("Level not found generating level edit menu!");
            return;
        }
        currentSelectedLevel = level;

        fillSpacers(editInv);
        ItemStack title = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta titleMeta = title.getItemMeta();
        titleMeta.displayName(Component.text(itemName).decoration(TextDecoration.ITALIC, false));
        title.setItemMeta(titleMeta);
        editInv.setItem(4, title);

        //default mechanic selectors
        //gameMode Selector
        ItemStack gameMode = new ItemStack(Material.PURPLE_CONCRETE);
        ItemMeta gameModeMeta = gameMode.getItemMeta();
        gameModeMeta.displayName(Component.text("Change Level GameMode").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> gameModeLore = new ArrayList<>();
        if(level.getGameMode() == GameMode.SURVIVAL) gameModeLore.add(Component.text("Survival").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN));
        else gameModeLore.add(Component.text("Survival").color(NamedTextColor.RED));
        if(level.getGameMode() == GameMode.ADVENTURE) gameModeLore.add(Component.text("Adventure").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN));
        else gameModeLore.add(Component.text("Adventure").color(NamedTextColor.RED));
        if(level.getGameMode() == GameMode.CREATIVE) gameModeLore.add(Component.text("Creative").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN));
        else gameModeLore.add(Component.text("Creative").color(NamedTextColor.RED));
        gameModeMeta.lore(gameModeLore);
        gameMode.setItemMeta(gameModeMeta);
        editInv.setItem(10, gameMode);

        //min players selector
        ItemStack minPlayers = new ItemStack(Material.PURPLE_CONCRETE);
        ItemMeta minPlayersMeta = minPlayers.getItemMeta();
        minPlayersMeta.displayName(Component.text("Change Minimum Players").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> minPlayersLore = new ArrayList<>();

        if(level.getMinPlayers() == 1) minPlayersLore.add(Component.text("1").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN));
        else minPlayersLore.add(Component.text("1").color(NamedTextColor.RED));
        if(level.getMinPlayers() == 2) minPlayersLore.add(Component.text("2").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN));
        else minPlayersLore.add(Component.text("2").color(NamedTextColor.RED));
        if(level.getMinPlayers() == 3) minPlayersLore.add(Component.text("3").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN));
        else minPlayersLore.add(Component.text("3").color(NamedTextColor.RED));
        if(level.getMinPlayers() == 4) minPlayersLore.add(Component.text("4").decorate(TextDecoration.BOLD).color(NamedTextColor.GREEN));
        else minPlayersLore.add(Component.text("4").color(NamedTextColor.RED));
        minPlayersMeta.lore(minPlayersLore);
        minPlayers.setItemMeta(minPlayersMeta);
        editInv.setItem(11, minPlayers);

        //environment damage toggle
        ItemStack envDamage;
        if(level.isEnvDamage()) {
            envDamage = new ItemStack(Material.LIME_CONCRETE);
        }
        else envDamage = new ItemStack(Material.RED_CONCRETE);
        ItemMeta envMeta = envDamage.getItemMeta();
        envMeta.displayName(Component.text("Toggle Environment Damage").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> envLore = new ArrayList<>(3);
        if(level.isEnvDamage()) envLore.add(Component.text("Currently Set to: true").color(NamedTextColor.GREEN));
        else envLore.add(Component.text("Currently Set to: false").color(NamedTextColor.RED));
        envLore.add(Component.text("Set whether players can take"));
        envLore.add(Component.text("damage from their environment."));
        envLore.add(Component.text("This includes fall/lava damage"));
        envLore.add(Component.text("and damage from mobs."));
        envMeta.lore(envLore);
        envDamage.setItemMeta(envMeta);
        editInv.setItem(12, envDamage);

        //pvp damage toggle
        ItemStack pvpDamage;
        if(level.isPvpDamage()) {
            pvpDamage = new ItemStack(Material.LIME_CONCRETE);
        }
        else pvpDamage = new ItemStack(Material.RED_CONCRETE);
        ItemMeta pvpMeta = pvpDamage.getItemMeta();
        pvpMeta.displayName(Component.text("Toggle PVP Damage").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        ArrayList<Component> pvpLore = new ArrayList<>(3);
        if(level.isPvpDamage()) pvpLore.add(Component.text("Currently Set to: true").color(NamedTextColor.GREEN));
        else pvpLore.add(Component.text("Currently Set to: false").color(NamedTextColor.RED));
        pvpLore.add(Component.text("Set whether players can take"));
        pvpLore.add(Component.text("damage from their teammates."));
        pvpMeta.lore(pvpLore);
        pvpDamage.setItemMeta(pvpMeta);
        editInv.setItem(13, pvpDamage);

        //set spawn
        ItemStack spawnSet = new ItemStack(Material.ENDER_PEARL);
        ItemMeta spawnSetMeta = spawnSet.getItemMeta();
        spawnSetMeta.displayName(Component.text("Set Level Spawn").decoration(TextDecoration.ITALIC, false));
        List<Component> spawnLore = new ArrayList<>(setPosLore(level.getSpawnPoint()));
        spawnLore.add(0, Component.text("Sets the level's spawn to your current location"));
        spawnSetMeta.lore(spawnLore);
        spawnSet.setItemMeta(spawnSetMeta);
        editInv.setItem(15, spawnSet);

        //set end button
        ItemStack endSet = new ItemStack(Material.OAK_BUTTON);
        ItemMeta endSetMeta = endSet.getItemMeta();
        endSetMeta.displayName(Component.text("Set Level End Button").decoration(TextDecoration.ITALIC, false));
        List<Component> endLore = new ArrayList<>(setPosLore(level.getSpawnPoint()));
        endLore.add(0, Component.text("Sets the level's spawn to your current location"));
        endSetMeta.lore(endLore);
        endSet.setItemMeta(endSetMeta);
        editInv.setItem(16, endSet);

        //mechanics, need to add here when adding a new mechanic
        int i = 18;
        for(Mechanic m: level.getMechanics()) {
            if(m != null) {
                editInv.setItem(i, m.createItem());
                i++;
            }
        }

        ItemStack pos1Rod = new ItemStack(Material.BREEZE_ROD);
        ItemMeta pos1RodMeta = pos1Rod.getItemMeta();
        pos1RodMeta.displayName(Component.text("Set Position 1").decoration(TextDecoration.ITALIC, false));
        pos1RodMeta.lore(setPosLore(level.getPos1()));
        pos1Rod.setItemMeta(pos1RodMeta);
        editInv.setItem(45, pos1Rod);

        ItemStack pos2Rod = new ItemStack(Material.BLAZE_ROD);
        ItemMeta pos2RodMeta = pos2Rod.getItemMeta();
        pos2RodMeta.displayName(Component.text("Set Position 2").decoration(TextDecoration.ITALIC, false));
        pos2RodMeta.lore(setPosLore(level.getPos2()));
        pos2Rod.setItemMeta(pos2RodMeta);
        editInv.setItem(46, pos2Rod);

        ItemStack addMechanic = new ItemStack(Material.NETHER_STAR);
        ItemMeta addMechanicMeta = addMechanic.getItemMeta();
        addMechanicMeta.displayName(Component.text("Add Mechanic").color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        addMechanic.setItemMeta(addMechanicMeta);
        editInv.setItem(49, addMechanic);

        ItemStack back = new ItemStack(Material.RED_CONCRETE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);
        editInv.setItem(50, back);

        ItemStack save = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta saveMeta = save.getItemMeta();
        saveMeta.displayName(Component.text("Save Level Schematic").color(NamedTextColor.DARK_GREEN).decoration(TextDecoration.ITALIC, false));
        save.setItemMeta(saveMeta);
        editInv.setItem(52, save);

        ItemStack delete = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.displayName(Component.text("Delete Level").color(NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
        delete.setItemMeta(deleteMeta);
        editInv.setItem(53, delete);

    }

    public Inventory generateAddMechanicMenu() {
        Inventory addMechanicMenu = Bukkit.createInventory(player, 54, Component.text("Select a Mechanic").decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GREEN));
        fillSpacers(addMechanicMenu);

        ArrayList<MechanicMapper.MechanicDetails> detailsList = MechanicMapper.getMechanics();
        int i = 9;
        for (MechanicMapper.MechanicDetails details: detailsList) {
            ItemStack mechanic = new ItemStack(details.mat);
            ItemMeta mechanicMeta = mechanic.getItemMeta();
            mechanicMeta.displayName(Component.text(details.name).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            mechanic.setItemMeta(mechanicMeta);
            addMechanicMenu.setItem(i, mechanic);
            i++;
        }

        ItemStack back = new ItemStack(Material.RED_CONCRETE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);
        editInv.setItem(53, back);

        return addMechanicMenu;
    }


    public void openDeleteLevelMenu(Level level) {
        Inventory deleteLevelMenu = Bukkit.createInventory(player, 18, Component.text("Delete Level?").decoration(TextDecoration.ITALIC, false)
                .color(NamedTextColor.GREEN));

        ItemStack data = new ItemStack(Material.BOOK);
        ItemMeta dataMeta = data.getItemMeta();
        dataMeta.displayName(Component.text(level.getName()).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        data.setItemMeta(dataMeta);
        deleteLevelMenu.setItem(4, data);

        ItemStack yes = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta yesMeta = data.getItemMeta();
        yesMeta.displayName(Component.text("Yes").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        yes.setItemMeta(yesMeta);
        deleteLevelMenu.setItem(11, yes);

        ItemStack no = new ItemStack(Material.RED_CONCRETE);
        ItemMeta noMeta = data.getItemMeta();
        noMeta.displayName(Component.text("No").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        no.setItemMeta(noMeta);
        deleteLevelMenu.setItem(15, no);

        player.openInventory(deleteLevelMenu);

    }

    public void openMainInv() {
        generateMainMenu();

        player.openInventory(mainInv);
        isOpen = true;
        currentGUI = GUI.MAIN;

    }

    public void openEditInv(String levelName) {
        generateLevelEditMenu(levelName);
        player.openInventory(editInv);
        isOpen = true;
        currentGUI = GUI.LEVEL_EDITOR;
    }

    public void openAddMechanicInv() {
        Inventory mechanicMenu = generateAddMechanicMenu();
        player.openInventory(mechanicMenu);
        isOpen = true;
        currentGUI = GUI.ADD_MECHANIC;
    }

    public void openEditMechanicInv(Mechanic mechanic) {
        System.out.println("mechanic" + mechanic);
        Inventory mechanicMenu = mechanic.generateMechanicSettingsMenu(player);
        player.openInventory(mechanicMenu);
        isOpen = true;
        currentGUI = GUI.MECHANIC_SETTINGS;

    }

    public void close() {
        isOpen = false;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public GUI getCurrentGUI() {
        return currentGUI;
    }

    public void makeNewLevel(EditorGUIHandler editorGUIHandler) {
        //first create anvil
        AtomicBoolean result = new AtomicBoolean(false);
        new AnvilGUI.Builder()
                .onClose(player1 -> {                                               //called when the inventory is closing
                    editorGUIHandler.refreshAll();
                    openMainInv();
                })
                .onClick((slot, stateSnapshot) -> {                                    //called when the inventory output slot is clicked
                    String mapName = stateSnapshot.getText().trim();

                    if(mapName.length() <= 20 && !(mapName.isEmpty())) {
                        result.set(true);
                    } else {
                        player.sendMessage("Level Name must be between 1 and 20 Characters.");
                        result.set(false);
                    }

                    if (result.get()) {
                        LevelHandler.getInstance().createLevel(player, mapName);
                    } else {
                        player.sendMessage("Could not Create Level");
                    }
                    return AnvilGUI.Response.close();
                })
                .text(" ")                                      //sets the text the GUI should start with
                .itemLeft(new ItemStack(Material.PAPER))        //use a custom item for the first slot
                .title("Enter Level Name:")                     //set the title of the GUI (only works in 1.14+)
                .plugin(plugin)                                 //set the plugin instance
                .open(player);                                  //opens the GUI for the player provided

    }

    private void fillSpacers(Inventory inv) {
        ItemStack border = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(""));
        border.setItemMeta(borderMeta);

        ItemStack filler = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
        filler.setItemMeta(borderMeta);

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, border);
        }
        for(int i = 9; i < 45; i++) {
            inv.setItem(i, filler);
        }
        for (int i = 45; i < 54; i++) {
            inv.setItem(i, border);
        }
    }

    private List<Component> setPosLore(Location levelPos) {
        List<Component> lore;
        if(levelPos != null) {
            lore = List.of(new TextComponent[]{
                    Component.text("Currently at"),
                    Component.text("X: " +( int)levelPos.getX()),
                    Component.text("Y: " +( int)levelPos.getY()),
                    Component.text("Z: " +( int)levelPos.getZ())});

        } else {
            lore = List.of(new TextComponent[]{Component.text("Location not set")});
        }
        return lore;
    }

    public enum GUI {
        MAIN,
        LEVEL_EDITOR,
        ADD_MECHANIC,
        MECHANIC_SETTINGS
    }
}
