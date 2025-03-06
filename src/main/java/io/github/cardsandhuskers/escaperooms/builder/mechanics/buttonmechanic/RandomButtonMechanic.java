package io.github.cardsandhuskers.escaperooms.builder.mechanics.buttonmechanic;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.listeners.ButtonMechanicClickListener;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.objects.EditorGUI;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Switch;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Used to select a series of locations that may have a button, then a button will be placed in one of them
 * Official type: Random Button Location
 */
public class RandomButtonMechanic extends Mechanic {

    private final ArrayList<BlockLocation> blockLocations = new ArrayList<>();

    private int randomNum = 0;

    public RandomButtonMechanic(Level level) {
        super();
        this.level = level;
    }

    public RandomButtonMechanic(String mechanicID, Level level, ConfigurationSection attributes) {

        this.mechanicID = UUID.fromString(mechanicID);
        this.level = level;


        List<Map<?, ?>> locations = (List<Map<?, ?>>) attributes.getList("locations");
        if (locations != null) {
            for (Map<?, ?> locationData : locations) {
                // Deserialize each location into a BlockLocation
                blockLocations.add(BlockLocation.deserialize((Map<String, Object>) locationData));
            }
        }

        randomNum = new Random().nextInt(blockLocations.size());
    }

    @Override
    public List<Component> getLore() {
        return List.of(Component.text("Currently " + blockLocations.size() + " saved locations."));
    }

    public boolean addLocation(Block block, BlockFace blockFace) {
        Vector buttonBlock = null;

        switch(blockFace) {
            case NORTH -> buttonBlock = new Vector(block.getX(), block.getY(), block.getZ() - 1);
            case SOUTH -> buttonBlock = new Vector(block.getX(), block.getY(), block.getZ() + 1);
            case EAST -> buttonBlock = new Vector(block.getX() + 1, block.getY(), block.getZ());
            case WEST -> buttonBlock = new Vector(block.getX() - 1, block.getY(), block.getZ());
            case UP -> buttonBlock = new Vector(block.getX(), block.getY() + 1, block.getZ());
            case DOWN -> buttonBlock = new Vector(block.getX(), block.getY() - 1, block.getZ());
        }

        if(buttonBlock == null) {
            System.out.println("BUTTON BLOCK IS NULL????");
            return false;
        }
        Vector diff = level.getDiffFromSchem(buttonBlock);
        if(diff == null) return false;

        BlockLocation bl = new BlockLocation(diff.getBlockX(), diff.getBlockY(), diff.getBlockZ(), blockFace);
        blockLocations.add(bl);
        randomNum = new Random().nextInt(blockLocations.size());

        return true;

    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        HashMap<String, Object> attributes = new HashMap<>();

        List<Map<String, Object>> serializedLocations = new ArrayList<>();

        for (BlockLocation loc : blockLocations) {
            serializedLocations.add(loc.serialize());
        }

        attributes.put("type", MechanicMapper.getMechName(this.getClass()));
        attributes.put("locations", serializedLocations);

        return attributes;
    }

    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {

        Inventory mechanicInv = Bukkit.createInventory(player, 54, Component.text("Mechanic: " + MechanicMapper.getMechName(this.getClass()))
                .color(NamedTextColor.BLUE));

        //ID item
        mechanicInv.setItem(4, createIDItem(mechanicID, Material.STONE_BUTTON));

        //location list
        int i = 9;
        for(BlockLocation location: blockLocations) {

            Vector position = level.getCoordsFromSchem(new Vector(location.getX(), location.getY(), location.getZ()));

            Component pearlName = Component.text("Location " + (i - 8)).decoration(TextDecoration.ITALIC, false);
            List<Component> pearlLore = List.of(
                    Component.text("X: " + position.getX()),
                    Component.text("Y: " + position.getY()),
                    Component.text("Z: " + position.getZ()),
                    Component.text("Face: " + location.getFace()),
                    Component.text("Right Click to Delete")
            );
            mechanicInv.setItem(i, createItem(Material.ENDER_PEARL, 1, pearlName, pearlLore));
            i++;
        }

        Component wandName = Component.text("Add Button Locations").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GREEN);
        mechanicInv.setItem(0, createItem(Material.BLAZE_ROD, 1, wandName, null));

        mechanicInv.setItem(45, EditorGUI.createBackButton());
        mechanicInv.setItem(53, EditorGUI.createDeleteButton());

        return mechanicInv;
    }

    @Override
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {
        Player p = (Player) e.getInventory().getHolder();
        EscapeRooms plugin = EscapeRooms.getPlugin();

        if (e.getClickedInventory() != null) {
            e.setCancelled(true);
            String itemName = PlainTextComponentSerializer.plainText().serialize(e.getCurrentItem().displayName());
            itemName = itemName.replaceAll("\\[|\\]", ""); // Removes "[" and "]"

            if(itemName.equalsIgnoreCase("Add Button Locations")) {

                p.getInventory().addItem(new ItemStack(Material.BLAZE_ROD));

                ButtonMechanicClickListener buttonMechanicClickListener = new ButtonMechanicClickListener(this, p, editorGUIHandler);
                buttonMechanicClickListener.startOperation();
                plugin.getServer().getPluginManager().registerEvents(buttonMechanicClickListener, plugin);

            } else if (e.getClick() == ClickType.RIGHT && e.getCurrentItem().getType() == Material.ENDER_PEARL) {
                int locIdx = itemName.charAt(itemName.length() - 1) - '0';
                blockLocations.remove(locIdx - 1);
                randomNum = new Random().nextInt(blockLocations.size());

                p.openInventory(generateMechanicSettingsMenu(p));
            }
        }
    }

    @Override
    public void eventHandler(TeamInstance teamInstance, Event e) {

    }

    /**
     * Picks a random block from the list and sets a button at it, then faces it correctly
     * @param teamInstance - teamInstance to use to get corners for button locations
     */
    @Override
    public void levelStartExecution(TeamInstance teamInstance) {
        Location corner = teamInstance.getCurrentLevelCorner();

        BlockLocation loc = blockLocations.get(randomNum);

        corner.add(loc.getX(), loc.getY(), loc.getZ());

        Block block = corner.getBlock();
        block.setType(Material.STONE_BUTTON);
        BlockData data = block.getBlockData();
        Switch buttonData = (Switch) data;
        buttonData.setFacing(loc.getFace());
        block.setBlockData(buttonData);

        //just set the level offset directly, it won't save to a file and that way I don't need to do any listening or anything
        level.setLevelEndButtonOffset(new Vector(loc.getX(), loc.getY(), loc.getZ()));
    }

}
