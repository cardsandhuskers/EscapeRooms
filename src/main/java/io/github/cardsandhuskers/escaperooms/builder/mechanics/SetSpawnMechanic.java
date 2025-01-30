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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
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
  private Vector relativeRespawn;

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
    this.relativeRespawn = new Vector(0,0,0);

    String itemStringX = attributes.getString("x");
    String itemStringY = attributes.getString("y");
    String itemStringZ = attributes.getString("z");

    if(itemStringX != null && !itemStringX.isEmpty()) {
      this.relativeRespawn.setX(Double.parseDouble(itemStringX));
    }

    if(itemStringY != null && !itemStringY.isEmpty()) {
      this.relativeRespawn.setY(Double.parseDouble(itemStringY));
    }

    if(itemStringZ != null && !itemStringZ.isEmpty()) {
      this.relativeRespawn.setZ(Double.parseDouble(itemStringZ));
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
    this.relativeRespawn = new Vector(0,0,0);
  }

  /**
   * Serializes respawnVector data
   * @return Map of Vector data
   */
  @Override
  public @NotNull Map<String, Object> serialize() {
    Map<String, Object> attributes = new HashMap<>();

    if(this.relativeRespawn == null) {
      attributes.put("spawnLocation", new Vector(0,0,0).serialize());
    }

    attributes.put("spawnLocation", this.relativeRespawn.serialize());

    attributes.put("type", MechanicMapper.getMechName(this.getClass()));

    return attributes;
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
    Vector vector = new Vector((int)location.getX(), (int)location.getY(), (int)location.getZ());
    this.relativeRespawn = level.getDiffFromSchem(vector);
    addSpawnMeta(clickedItem);
  }

  /**
   * Adds meta data to bed gui item if enabled
   * @param item
   * @return
   */
  private ItemStack addSpawnMeta(ItemStack item) {
    if(this.relativeRespawn == null) return item;

    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.displayName(Component.text("Respawn Point: " +
        level.getCoordsFromSchem(this.relativeRespawn).toString()));
    itemMeta.addEnchant(Enchantment.LURE, 1, enabled);
    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(itemMeta);

    return item;
  }

  @Override
  public void eventHandler(TeamInstance teamInstance, Event e) {
    if(e instanceof PlayerMoveEvent playerMoveEvent) {
      Player p = playerMoveEvent.getPlayer();

      Vector actualCheckpoint = teamInstance.getCurrentLevelCorner().toVector();
      level.getCoordsFromSchem(relativeRespawn);
      actualCheckpoint.setX(actualCheckpoint.getX() + relativeRespawn.getX());
      actualCheckpoint.setY(actualCheckpoint.getY() + relativeRespawn.getY());
      actualCheckpoint.setZ(actualCheckpoint.getZ() + relativeRespawn.getZ());

      Vector playerVector = p.getLocation().toVector();
      boolean check = true;

      if(playerVector.getX() > (actualCheckpoint.getX() + 1.5) ||
          playerVector.getX() < (actualCheckpoint.getX() - 0.5)) {
        check = false;
      } else if(playerVector.getZ() > (actualCheckpoint.getZ() + 1.5) ||
              playerVector.getZ() < (actualCheckpoint.getZ() - 0.5)) {
        check = false;
      }

      if(check) {
        p.setRespawnLocation(actualCheckpoint.toLocation(p.getWorld()));
      }

    } else if(e instanceof PlayerRespawnEvent deathEvent) {
      System.out.println("Damn, that sucks...");
      Vector actualCheckpoint = teamInstance.getCurrentLevelCorner().toVector();
      level.getCoordsFromSchem(relativeRespawn);
      actualCheckpoint.setX(actualCheckpoint.getX() + relativeRespawn.getX());
      actualCheckpoint.setY(actualCheckpoint.getY() + relativeRespawn.getY());
      actualCheckpoint.setZ(actualCheckpoint.getZ() + relativeRespawn.getZ());

      ((PlayerRespawnEvent) e).getPlayer().setRespawnLocation(actualCheckpoint.toLocation(((PlayerRespawnEvent) e).getPlayer().getWorld()));


    } else {
      System.out.println(e.toString());
    }

    //player death event]
    //check where standing to change checkpoint
    //check location of death for respawn


    /*
    grab the team instance, .getlevelcornercurrent (current), add offsets to that (from saved get diff function val)
    move event to see if player is in checkpoint region, then update player val
    save if player is in region
     */
  }

  @Override
  public void levelStartExecution(TeamInstance teamInstance) {
    return;
  }
}
