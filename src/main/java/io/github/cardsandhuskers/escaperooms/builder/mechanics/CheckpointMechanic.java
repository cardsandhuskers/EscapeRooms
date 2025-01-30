package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import java.util.*;

import io.github.cardsandhuskers.escaperooms.builder.objects.EditorGUI;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
 * @author jscotty
 */
public class CheckpointMechanic extends Mechanic{
  private Vector relativeRespawn;
  private HashMap<UUID,Integer> playerCheckpoint;
  private ArrayList<Vector> relativeCheckpoints;
  private ArrayList<Vector> actualCheckpoints;

  private static final int CHECKPOINT_GUI_OFFSET = 27;

  /**
   * Reads in metadata for relative spawn vectors and
   * stores them in the relativeCheckpoints list.
   * @param mechanicID
   * @param level
   * @param attributes
   */
  public CheckpointMechanic(String mechanicID, Level level, ConfigurationSection attributes) {
    this.mechanicID = UUID.fromString(mechanicID);
    this.level = level;
    relativeRespawn = new Vector(0,0,0);
    playerCheckpoint = new HashMap<>();
    relativeCheckpoints = new ArrayList<>();
    actualCheckpoints = new ArrayList<>();

    String size = attributes.getString("checkpointAmt");
    int amt = 0;

    if(size != null && !size.isEmpty()) {
      amt = Integer.parseInt(size);
    }

    for(int i = 0; i < amt; i++) {
      Vector vector = new Vector(0,0,0);

      String itemStringX = attributes.getString("LX" + i);
      String itemStringY = attributes.getString("LY" + i);
      String itemStringZ = attributes.getString("LZ" + i);

      if(itemStringX != null && !itemStringX.isEmpty()) {
        vector.setX(Double.parseDouble(itemStringX));
      }

      if(itemStringY != null && !itemStringY.isEmpty()) {
        vector.setY(Double.parseDouble(itemStringY));
      }

      if(itemStringZ != null && !itemStringZ.isEmpty()) {
        vector.setZ(Double.parseDouble(itemStringZ));
      }

      relativeCheckpoints.add(vector);
    }
  }

  /**
   * Constructor for newly created SetSpawnMechanic
   * @param level
   */
  public CheckpointMechanic(Level level) {
    super();
    this.level = level;
    relativeRespawn = new Vector(0,0,0);
    playerCheckpoint = new HashMap<>();
    relativeCheckpoints = new ArrayList<>();
    actualCheckpoints = new ArrayList<>();
  }

  /**
   * Serializes all relative checkpoints
   * @return Map of Vector data
   */
  @Override
  public @NotNull Map<String, Object> serialize() {
    Map<String, Object> attributes = new HashMap<>();

    for(int i = 0; i < relativeCheckpoints.size(); ++i) {
      attributes.put("LX" + i, relativeCheckpoints.get(i).getX());
      attributes.put("LY" + i, relativeCheckpoints.get(i).getY());
      attributes.put("LZ" + i, relativeCheckpoints.get(i).getZ());
    }

    attributes.put("checkpointAmt", relativeCheckpoints.size());
    attributes.put("type", MechanicMapper.getMechName(this.getClass()));

    return attributes;
  }

  /**
   * Creates a menu that contains a blue bed to add
   * a new checkpoint to the level. Purple beds
   * display all elements stored in a level.
   *
   * @param player - player that's opening the inventory
   * @return Inventory of player for gui
   */
  @Override
  public Inventory generateMechanicSettingsMenu(Player player) {
    Inventory mechanicInv = Bukkit.createInventory(player, 54, Component.text("Mechanic: " + 
      MechanicMapper.getMechName(this.getClass())).color(NamedTextColor.BLUE));

    mechanicInv.setItem(4, createIDItem(mechanicID, Material.BLUE_BED));

    //checkpoint nodes
    for(int i = 0; i < relativeCheckpoints.size(); i++) {
      ItemStack item = new ItemStack(Material.PURPLE_BED);
      addSpawnMeta(item,relativeCheckpoints.get(i));
      mechanicInv.setItem(CHECKPOINT_GUI_OFFSET + i, item);
    }

    //pop button
    ItemStack pop = new ItemStack(Material.BLACK_CONCRETE);
    ItemMeta popMeta = pop.getItemMeta();
    popMeta.displayName(Component.text("Pop").color(NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
    pop.setItemMeta(popMeta);
    mechanicInv.setItem(49, pop);

    //back/delete buttons
    mechanicInv.setItem(51, EditorGUI.createBackButton());
    mechanicInv.setItem(53, EditorGUI.createDeleteButton());

    return mechanicInv;
  }

  /**
   * Adds lore to indicate function of mechanic in GUI
   * @return lore
   */
  @Override
  public List<Component> getLore() {
    return List.of(Component.text("Select to set spawn point."));
  }

  /**
   * Clicking the blue bed creates a checkpoint and purple
   * bed in the gui, which displays metadata for the
   * checkpoint. Clicking black concrete removes the last
   * element in the checkpoint list.
   * @param e inventory click event
   * @param editorGUIHandler gui editor
   */
  @Override
  public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {
    e.setCancelled(true);
    ItemStack clickedItem = e.getCurrentItem();

    if(clickedItem == null) return;

    if(clickedItem.getType() == Material.PURPLE_BED) return;

    //POP functionality
    if(clickedItem.getType() == Material.BLACK_CONCRETE) {
      relativeCheckpoints.removeLast();
      return;
    }

    //adding relative checkpoint to list
    Player p = (Player) e.getWhoClicked();
    Location location = p.getLocation();
    Vector vector = new Vector((int)location.getX(), (int)location.getY(), (int)location.getZ());
    relativeCheckpoints.add(level.getDiffFromSchem(vector));

    //adding new checkpoint data to GUI
    ItemStack spawn = new ItemStack(Material.PURPLE_BED);
    addSpawnMeta(spawn,relativeCheckpoints.getLast());
    Inventory mechanicInv = e.getInventory();
    mechanicInv.setItem(CHECKPOINT_GUI_OFFSET + relativeCheckpoints.size(), spawn);
    p.updateInventory();
  }

  /**
   * Adds metadata for each checkpoint
   * @param item
   * @param vector
   */
  private void addSpawnMeta(ItemStack item, Vector vector) {
    if(this.relativeRespawn == null) return;

    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.displayName(Component.text("Respawn Point: " +
            vector.toString()));
    itemMeta.addEnchant(Enchantment.LURE, 1, true);
    itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(itemMeta);
  }

  /**
   * On PlayerMoveEvent, checks if player is in the general
   * location of a checkpoint. On PlayerRespawnEvent, sets
   * players respawn point to current checkpoint.
   * @param teamInstance team instance
   * @param e listener event
   */
  @Override
  public void eventHandler(TeamInstance teamInstance, Event e) {
    if(e instanceof PlayerMoveEvent playerMoveEvent) {
      Player p = playerMoveEvent.getPlayer();
      Vector playerVector = p.getLocation().toVector();
      int checkpointNum = playerCheckpoint.get(p.getUniqueId());
      boolean changeCheckpoint = false;

      if(checkpointNum == actualCheckpoints.size() - 1) return;

      checkpointNum++; //no checkpoint = -1

      //checking if player is within half a block of checkpoint
      for(int i = checkpointNum; i < actualCheckpoints.size(); i++) {
        Vector actualCheckpoint = actualCheckpoints.get(i);
        if(playerVector.getX() <= (actualCheckpoint.getX() + 1.5) &&
              playerVector.getX() >= (actualCheckpoint.getX() - 0.5) &&
              playerVector.getZ() <= (actualCheckpoint.getZ() + 1.5) &&
              playerVector.getZ() >= (actualCheckpoint.getZ() - 0.5)) {
            changeCheckpoint = true;
            break;
        }
      }

      //setting player checkpoint if checkpoint should change
      if(changeCheckpoint) {
        playerCheckpoint.put(p.getUniqueId(), checkpointNum);
      }

    } else if(e instanceof PlayerRespawnEvent deathEvent) {
      int checkpointNum = playerCheckpoint.get(deathEvent.getPlayer().getUniqueId());

      //no checkpoint stored
      if(checkpointNum < 0 || checkpointNum >= actualCheckpoints.size()) {
        return;
      }

      Vector v = actualCheckpoints.get(checkpointNum);
      deathEvent.setRespawnLocation(v.toLocation(deathEvent.getPlayer().getWorld()));
    } else {
      System.out.println(e.toString());
    }
  }

  /**
   * Generates actual check points based off of relative vector
   * checkpoint points and clears all player checkpoints.
   * @param teamInstance instance of team
   */
  @Override
  public void levelStartExecution(TeamInstance teamInstance) {
    Vector cornerVector = teamInstance.getCurrentLevelCorner().toVector();

    for(Vector v: relativeCheckpoints) {
      Vector actualVector = v.clone();
      actualVector.setX(cornerVector.getX() + v.getX());
      actualVector.setY(cornerVector.getY() + v.getY());
      actualVector.setZ(cornerVector.getZ() + v.getZ());

      actualCheckpoints.add(actualVector);
    }

    for(OfflinePlayer p: teamInstance.getTeam().getPlayers()) {
      playerCheckpoint.put(p.getUniqueId(),-1);
    }
  }
}
