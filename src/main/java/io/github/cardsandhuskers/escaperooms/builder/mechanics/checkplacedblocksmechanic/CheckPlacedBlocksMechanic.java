package io.github.cardsandhuskers.escaperooms.builder.mechanics.checkplacedblocksmechanic;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.buttonmechanic.BlockLocationData;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class CheckPlacedBlocksMechanic extends Mechanic {
    List<BlockPlacementData> blocksToCheck;
    Block buttonBlock;
    SuccessScenario successScenario;

    public CheckPlacedBlocksMechanic(Level level) {
        super();
        this.level = level;

        blocksToCheck = new ArrayList<>();
    }

    public CheckPlacedBlocksMechanic(String mechanicID, Level level, ConfigurationSection attributes) {
        this.mechanicID = UUID.fromString(mechanicID);
        this.level = level;

        //populate from attributes

    }

    public void checkBlocks(TeamInstance teamInstance) {
        if (allCorrect(teamInstance)) {
            triggerSuccessScenario();
        }
    }

    public boolean allCorrect(TeamInstance teamInstance) {
        for (BlockPlacementData bpd: blocksToCheck) {
                Block checkBlock = teamInstance.getLocationFromOffset(bpd.getVector()).getBlock();


            if (checkBlock.getType() != bpd.getMaterial()) {
                //failure
                return false;
            }
        }
        return true;
    }

    public void triggerSuccessScenario() {

    }

    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {
        Inventory mechanicInv = Bukkit.createInventory(player, 54, Component.text("Mechanic: " + MechanicMapper.getMechName(this.getClass()))
                .color(NamedTextColor.BLUE));

        mechanicInv.setItem(4, createIDItem(mechanicID, MechanicMapper.getMechMaterial(this.getClass())));

        int i = 18;
        for(BlockPlacementData bpd: blocksToCheck) {
            Vector schemCoords = getLevel().getCoordsFromSchem(bpd.getVector());

            List<Component> blockStackLore = List.of(
                    Component.text("Right click to delete."),
                    Component.text("Location:"),
                    Component.text("X: " + schemCoords.getBlockX()),
                    Component.text("Y: " + schemCoords.getBlockY()),
                    Component.text("Z: " + schemCoords.getBlockZ())
            );
            Component blockStackName = Component.text("Block: " + i).decoration(ITALIC, false);
            mechanicInv.setItem(i, Mechanic.createItem(bpd.getMaterial(), 1, blockStackName, blockStackLore));
            i++;
        }

        mechanicInv.setItem(9, createItem(Material.LIME_CONCRETE, 1,
                Component.text("Add Block").decoration(ITALIC, false), null));

        mechanicInv.setItem(11, createItem(Material.CYAN_CONCRETE, 1,
                Component.text("Set Success Behavior").decoration(ITALIC, false), null));

        List<Component> setButtonLore = List.of(
                Component.text("Sets the button for the player"),
                Component.text("to press to check the set blocks.")
        );
        mechanicInv.setItem(13, createItem(Material.BIRCH_BUTTON, 1,
                Component.text("Set Check Button"), setButtonLore));

        mechanicInv.setItem(45, EditorGUI.createBackButton());
        mechanicInv.setItem(53, EditorGUI.createDeleteButton());

        return mechanicInv;
    }

    @Override
    public List<Component> getLore() {
        return null;
    }

    @Override
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {

        Player p = (Player) e.getInventory().getHolder();
        if (e.getClickedInventory() != null && e.getClickedInventory() != p.getInventory()) {
            e.setCancelled(true);
            String itemName = PlainTextComponentSerializer.plainText().serialize(e.getCurrentItem().displayName());

            //List<Component> itemLore = e.getCurrentItem().lore();

            if (itemName.contains("Block: ")) {
                //get 'index'
                try {
                    int num = Integer.parseInt(itemName.split(":")[1].strip());
                    if(e.isRightClick()) {
                        blocksToCheck.remove(num - 1);
                    }


                } catch (NumberFormatException ex) {
                    //
                }
            }

            if (itemName.equals("Set Check Button")) {
                Block feetBlock = p.getLocation().getBlock();
                Block headBlock = p.getLocation().add(0,1,0).getBlock();

                if(Level.isButton(feetBlock.getType())) {
                    buttonBlock = feetBlock;
                } else if(Level.isButton(headBlock.getType())) {
                    buttonBlock = headBlock;
                }
            }

        }
    }

    @Override
    public void eventHandler(TeamInstance teamInstance, Event e) {
        if (e instanceof PlayerInteractEvent pie) {
            Block b = pie.getClickedBlock();
            if (b != null) {
                if (Level.isButton(b.getType())) {
                    checkBlocks(teamInstance);
                }
            }
        }
    }

    @Override
    public void levelStartExecution(TeamInstance teamInstance) {

    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> attributes = new HashMap<>();
        if (buttonBlock != null) {
            attributes.put("buttonBlock", buttonBlock);
        }
        if (successScenario != null) {
            attributes.put("successScenario", successScenario.serialize());
        }

        return attributes;
    }
}
