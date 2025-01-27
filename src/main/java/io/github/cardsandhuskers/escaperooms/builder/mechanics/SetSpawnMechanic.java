package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import java.util.*;

import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

/**
 * Class is used to set a respawn point for a level. Respawn points
 * are relative to the level corners (which must be set for respawn
 * point to be stored correctly).
 * @author jscot
 */
public class SetSpawnMechanic extends Mechanic{
  private Boolean enabled;
  private Vector respawnVector;

/**
   * Constructor for when mechannic is read in from file.
   * Should always pass in those 3 things
   * @param mechanicID
   * @param level
   * @param attributes
   */
  public SetSpawnMechanic(String mechanicID, Level level, ConfigurationSection attributes) {
    this.mechanicID = UUID.fromString(mechanicID);
    this.level = level;
    this.enabled = false;
    this.respawnVector = new Vector(0,0,0);

    String itemStringX = attributes.getString("x");
    String itemStringY = attributes.getString("y");
    String itemStringZ = attributes.getString("z");

    if(itemStringX != null && !itemStringX.isEmpty()) {
      this.respawnVector.setX(Double.parseDouble(itemStringX));
    }

    if(itemStringY != null && !itemStringY.isEmpty()) {
      this.respawnVector.setY(Double.parseDouble(itemStringY));
    }

    if(itemStringZ != null && !itemStringZ.isEmpty()) {
      this.respawnVector.setZ(Double.parseDouble(itemStringZ));
    }
  }

  /**
   * Constructor for newly created SetSpawnMechanic
   * @param level
   */
  public SetSpawnMechanic(Level level) {
    super();
    this.level = level;
    this.enabled = false;
    this.respawnVector = new Vector(0,0,0);
  }

  /**
   * Serializes respawnVector data
   * @return Map of Vector data
   */
  @Override
  public @NotNull Map<String, Object> serialize() {
    if(this.respawnVector == null) {
      return new Vector(0,0,0).serialize();
    }
    return this.respawnVector.serialize();
  }

  /**
   * Creates menu with bed to set current respawn, and shows
   * any respawn points that are currently saved.
   *
   * @param player - player that's opening the inventory
   * @return Inventory of player for gui
   */
  @Override
  public Inventory generateMechanicSettingsMenu(Player player) {
    Inventory mechanicInv = Bukkit.createInventory(player, 54, Component.text("Mechanic: " + 
      MechanicMapper.getMechName(this.getClass())).color(NamedTextColor.BLUE));

    mechanicInv.setItem(4, addSpawnMeta(createIDItem(mechanicID, Material.BLUE_BED)));


    //back button
    ItemStack back = new ItemStack(Material.RED_CONCRETE);
    ItemMeta backMeta = back.getItemMeta();
    backMeta.displayName(Component.text("Back").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
    back.setItemMeta(backMeta);
    mechanicInv.setItem(51, back);

    //delete button
    ItemStack delete = new ItemStack(Material.BARRIER);
    ItemMeta deleteMeta = delete.getItemMeta();
    deleteMeta.displayName(Component.text("Delete Mechanic").
            color(NamedTextColor.DARK_RED).decoration(TextDecoration.ITALIC, false));
    delete.setItemMeta(deleteMeta);
    mechanicInv.setItem(53, delete);

    return mechanicInv;
  }

  @Override
  public List<Component> getLore() {
    return List.of(Component.text("Select to set spawn point."));
  }

  /**
   * Handles click of Blue Bed to set spawn
   * @param e
   * @param editorGUIHandler
   */
  @Override
  public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {
    e.setCancelled(true);
    ItemStack clickedItem = e.getCurrentItem();

    //relative respawn point from level corners
    Player p = (Player) e.getWhoClicked();
    Location location = p.getLocation();
    Vector vector = new Vector(location.getX(), location.getY(), location.getZ());
    this.respawnVector = level.getDiffFromSchem(vector);
    addSpawnMeta(clickedItem);
  }

  /**
   * Adds meta data to bed gui item if enabled
   * @param item
   * @return
   */
  private ItemStack addSpawnMeta(ItemStack item) {
    if(this.respawnVector == null) return item;

    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.setDisplayName("Respawn Point: " + level.getCoordsFromSchem(this.respawnVector).toString());
    itemMeta.addEnchant(Enchantment.LURE, 1, enabled);
    item.setItemMeta(itemMeta);

    return item;
  }

  @Override
  public void eventHandler(TeamInstance teamInstance, Event e) {
      for(Player p: teamInstance.getTeam().getOnlinePlayers()) {
        p.setRespawnLocation(level.getCoordsFromSchem(this.respawnVector).toLocation(p.getWorld()));
      }
  }

  @Override
  public void levelStartExecution(TeamInstance teamInstance) {
    for(Player p: teamInstance.getTeam().getOnlinePlayers()) {
      p.setRespawnLocation(level.getCoordsFromSchem(this.respawnVector).toLocation(p.getWorld()));
    }
  }
}
