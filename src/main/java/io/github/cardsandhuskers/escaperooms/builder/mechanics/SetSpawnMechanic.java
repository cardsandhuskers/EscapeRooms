package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class SetSpawnMechanic extends Mechanic{
  private Boolean enabled;

/**
   * Constructor for when read in from file.
   * Should always pass in those 3 things
   * @param mechanicID
   * @param level
   * @param attributes
   */
  public SetSpawnMechanic(String mechanicID, Level level, ConfigurationSection attributes) {
      this.mechanicID = UUID.fromString(mechanicID);
      this.level = level;
      this.enabled = false;
  }

  public SetSpawnMechanic(Level level) {
    super();
    this.level = level;
    this.enabled = false;
  }

  @Override
  public Map<String, Object> getData() {
    Map<String,Object> map = new HashMap<String, Object>();
    map.put("enabled",enabled);

    return map;
  }

  @Override
  public Inventory generateMechanicSettingsMenu(Player player) {
    Inventory mechanicInv = Bukkit.createInventory(player, 54, Component.text("Mechanic: " + 
      MechanicMapper.getMechName(this.getClass())).color(NamedTextColor.BLUE));

    mechanicInv.setItem(4, createIDItem(mechanicID, Material.BLUE_BED));


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
  public ItemStack createItem() {

    Material mat = MechanicMapper.getMechMaterial(this.getClass());
    ItemStack mechanicStack = new ItemStack(mat);

    List<Component> explanationLore = List.of(Component.text("Select to set spawn point."));
    ItemMeta mechanicMeta = mechanicStack.getItemMeta();
    Mechanic.embedUUID(mechanicMeta, mechanicID);
    mechanicMeta.displayName(Component.text(MechanicMapper.getMechName(this.getClass())).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
    mechanicMeta.lore(explanationLore);
    mechanicStack.setItemMeta(mechanicMeta);

    return mechanicStack;
  }

  @Override
  public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {
    e.setCancelled(true);

    ItemStack clickedItem = e.getCurrentItem();

    if(clickedItem != null && e.getSlot() != 4 && clickedItem.getType() == Material.BLUE_BED) {
      if(e.getClick() == ClickType.LEFT) {
        enabled = !enabled;
        ItemMeta clickedItemMeta = clickedItem.getItemMeta();
        clickedItemMeta.addEnchant(Enchantment.LURE, 1, enabled);
        clickedItem.setItemMeta(clickedItemMeta);

        Player p = (Player) e.getWhoClicked();
        Location location = p.getLocation();
        Vector vector = new Vector(location.getX(), location.getY(), location.getZ());

        Vector relativeVector = level.getDiff(vector);

        //level.getDiff -> vector(x,y,z); gets diff vector between corner of schedmatic and where you've put it
      }
    }

    //
    // p.setRespawnLocation(p.getLocation());
    // p.sendMessage("Spawn Point Set!");
  }
  
}
