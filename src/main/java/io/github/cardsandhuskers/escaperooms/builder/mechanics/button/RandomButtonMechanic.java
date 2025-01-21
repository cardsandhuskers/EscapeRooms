package io.github.cardsandhuskers.escaperooms.builder.mechanics.button;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.listeners.ButtonMechanicClickListener;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Used to select a series of locations that may have a button, then a button will be placed in one of them
 * Official type: Random Button Location
 */
public class RandomButtonMechanic extends Mechanic {

    private ArrayList<BlockLocation> blockLocations = new ArrayList<>();

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

    }

    @Override
    public List<Component> getLore() {
        List<Component> explanationLore = List.of(Component.text("Currently " + blockLocations.size() + " saved locations."));
        return explanationLore;
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
        Vector diff = level.getDiff(buttonBlock);
        if(diff == null) return false;

        BlockLocation bl = new BlockLocation(diff.getBlockX(), diff.getBlockY(), diff.getBlockZ(), blockFace);
        blockLocations.add(bl);

        return true;

    }

    @Override
    public Map<String, Object> getData() {
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

            Vector position = level.getCoords(new Vector(location.getX(), location.getY(), location.getZ()));

            ItemStack item = new ItemStack(Material.ENDER_PEARL);
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.displayName(Component.text("").decoration(TextDecoration.ITALIC, false));

            itemMeta.lore(List.of(
                    Component.text("X: " + position.getX()),
                    Component.text("Y: " + position.getY()),
                    Component.text("Z: " + position.getZ()),
                    Component.text("Face: " + location.getFace())
            ));
            item.setItemMeta(itemMeta);
            mechanicInv.setItem(i, item);
            i++;
        }

        ItemStack wand = new ItemStack(Material.BLAZE_ROD);
        ItemMeta wandMeta = wand.getItemMeta();
        wandMeta.displayName(Component.text("Add Button Locations").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_GREEN));
        wand.setItemMeta(wandMeta);
        mechanicInv.setItem(45, wand);

        ItemStack back = new ItemStack(Material.RED_CONCRETE);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);
        mechanicInv.setItem(51, back);

        ItemStack delete = new ItemStack(Material.BARRIER);
        ItemMeta deleteMeta = delete.getItemMeta();
        deleteMeta.displayName(Component.text("Delete Mechanic").color(NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
        delete.setItemMeta(deleteMeta);
        mechanicInv.setItem(53, delete);

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



            }
        }
    }

    @Override
    public void eventHandler(TeamInstance teamInstance, Event e) {

    }

    @Override
    public void levelStartExecution(TeamInstance teamInstance) {

    }

}
