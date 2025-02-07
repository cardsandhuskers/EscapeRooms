package io.github.cardsandhuskers.escaperooms.builder.mechanics.potionmechanic;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.objects.EditorGUI;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Allows for the application of 1 or more potion effects to start players with on a level
 */
public class PotionEffectsMechanic extends Mechanic {

    private final HashMap<PotionEffectType, PotionInfo> potions = new HashMap<>();

    /**
     * Constructor for when instantiated brand new
     * Should only pass level, must call super()
     * @param level - level mechanic is attached to
     */
    public PotionEffectsMechanic(Level level) {
        super();
        this.level = level;

        for (PotionEffectType type : PotionEffectType.values()) {
            if(isSkip(type)) continue;
            potions.put(type, new PotionInfo(type, 0, false));
        }

    }

    /**
     * Constructor for when read in from file.
     * Assigns mechanic id and level, then parses the attributes ConfigurationSection to get the mechanic's data
     * Should always pass in those 3 things
     * does NOT call super()
     * @param mechanicID - unique ID of the mechanic
     * @param level - level mechanic is attached to
     * @param attributes - list of attributes the mechanic has
     */
    public PotionEffectsMechanic(String mechanicID, Level level, ConfigurationSection attributes) {

        this.mechanicID = UUID.fromString(mechanicID);
        this.level = level;

        //TODO: Apply Attributes
        List<Map<?, ?>> potionMap = (List<Map<?, ?>>) attributes.getList("potions");
        if(potionMap != null) {
            for(Map<?, ?> potionData: potionMap) {
                PotionInfo info = PotionInfo.deserialize((Map<String, Object>) potionData);
                potions.put(info.effectType, info);
            }
        }
    }

    /**
     * Serializes the data into a hashmap
     * @return - the hashmap
     */
    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("type", MechanicMapper.getMechName(this.getClass()));

        List<Map<String, Object>> serializedPotions = new ArrayList<>();
        for (PotionInfo pot: potions.values()) {
            serializedPotions.add(pot.serialize());
        }
        attributes.put("potions", serializedPotions);

        return attributes;
    }

    /**
     * Creates the lore for the item in the level editor menu
     * @return - component list for the lore
     */
    @Override
    public List<Component> getLore() {

        ArrayList<Component> explanationLore = new ArrayList<>();

        explanationLore.add(Component.text("Potion effects applied at level start:"));
        for(PotionInfo potionInfo: potions.values()) {
            if(potionInfo.isEnabled) explanationLore.add(Component.text(parseEffectName(potionInfo.effectType)));
        }

       return explanationLore;
    }

    /**
     * Generates the settings menu for the mechanic
     * @param player - player to generate mechanic for
     * @return - Inventory object to open
     */
    @Override
    public Inventory generateMechanicSettingsMenu(Player player) {

        Inventory mechanicInv = Bukkit.createInventory(player, 54, Component.text("Mechanic: " + MechanicMapper.getMechName(this.getClass()))
                .color(NamedTextColor.BLUE));

        ItemStack idItem = createIDItem(mechanicID, Material.POTION);
        ItemMeta idMeta = idItem.getItemMeta();
        idMeta.lore(List.of(Component.text("Left Click a potion to toggle enabling it"), Component.text("Right Click a potion to edit its level")));
        idItem.setItemMeta(idMeta);
        mechanicInv.setItem(4, idItem);

        int i = 9;
        for (PotionInfo potionInfo : potions.values()) {

            ItemStack potion = new ItemStack(Material.POTION);
            PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();

            potionMeta.displayName(Component.text(parseEffectName(potionInfo.effectType)).decoration(TextDecoration.ITALIC, false));
            PotionEffect potionEffect = new PotionEffect(potionInfo.effectType, -1, potionInfo.amplifier);  // default to amplifier 1
            potionMeta.addCustomEffect(potionEffect, true);
            potionMeta.setColor(getPotionColorForEffect(potionInfo.effectType));

            List<TextComponent> lore = new ArrayList<>();
            if(potionInfo.isEnabled) {
                potionMeta.addEnchant(Enchantment.LURE, 1, false);
                potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                lore.add(Component.text("Selected").color(NamedTextColor.GREEN));

            } else {
                lore.add(Component.text("Not Selected").color(NamedTextColor.RED));
            }
            potionMeta.lore(lore);

            potion.setItemMeta(potionMeta);
            mechanicInv.setItem(i, potion);
            i++;
        }

        mechanicInv.setItem(45, EditorGUI.createBackButton());
        mechanicInv.setItem(53, EditorGUI.createDeleteButton());

        return mechanicInv;

    }

    /**
     * handles the click event when the clicked inventory is the one for this mechanic
     * @param e - event for the inventory click
     * @param editorGUIHandler - kind of a singleton, has data about player and inventory stuff
     */
    @Override
    public void handleClick(InventoryClickEvent e, EditorGUIHandler editorGUIHandler) {

        e.setCancelled(true);
        ItemStack clickedItem = e.getCurrentItem();

        if(clickedItem != null && e.getSlot() != 4 && clickedItem.getType() == Material.POTION) {

            PotionMeta meta = (PotionMeta) clickedItem.getItemMeta();

            if(e.getClick() == ClickType.LEFT) {
                if (meta.hasCustomEffects()) {
                    // Iterate over custom effects
                    for (PotionEffect effect : meta.getCustomEffects()) {
                        PotionEffectType type = effect.getType();

                        PotionInfo info = potions.get(type);
                        info.isEnabled = !info.isEnabled;
                        getLevel().writeData();
                    }

                    e.getWhoClicked().openInventory(generateMechanicSettingsMenu((Player) e.getWhoClicked()));
                }

            } else if (e.getClick() == ClickType.RIGHT){

                if (meta.hasCustomEffects()) {
                    // Iterate over custom effects
                    for (PotionEffect effect : meta.getCustomEffects()) {
                        PotionEffectType type = effect.getType();
                        PotionInfo info = potions.get(type);

                        createAnvilInput((Player) e.getWhoClicked(), info);
                    }
                }
            }
        }

    }

    @Override
    public void eventHandler(TeamInstance teamInstance, Event e) {

    }

    @Override
    public void levelStartExecution(TeamInstance teamInstance) {
        for(Player p: teamInstance.getTeam().getOnlinePlayers()) {
            for(PotionEffectType effect:potions.keySet()) {
                PotionInfo info = potions.get(effect);
                if(info.isEnabled) {
                    p.addPotionEffect(new PotionEffect(info.effectType, PotionEffect.INFINITE_DURATION, info.amplifier));
                }
            }
        }
    }

    private void createAnvilInput(Player player, PotionInfo info) {
        //first create anvil
        AtomicBoolean result = new AtomicBoolean(false);
        new AnvilGUI.Builder()
                .onClose(player1 -> {           //called when the inventory is closing
                    getLevel().writeData();
                    player.openInventory(generateMechanicSettingsMenu(player));
                })
                .onClick((slot, stateSnapshot) -> {         //called when the inventory output slot is clicked
                    String input = stateSnapshot.getText().trim();
                    int level = 0;
                    try {
                        level = Integer.parseInt(input);
                        if(level >= 1 && level <= 256) {
                            result.set(true);
                        } else {
                            result.set(false);
                        }
                    } catch (Exception e) {
                        result.set(false);
                    }

                    if (result.get()) {
                        //amplifier is 0 indexed, we give them 1 indexed numbers, but should keep 0 indexed in the backend
                        info.amplifier = level - 1;
                    } else {
                        player.sendMessage("Potion level must be an integer between 1 and 256.");
                    }
                    return AnvilGUI.Response.close();
                })
                .text(" ")                                      //sets the text the GUI should start with
                .itemLeft(new ItemStack(Material.PAPER))        //use a custom item for the first slot
                .title("Enter Potion Level (1-256) for : " + parseEffectName(info.effectType)) //set the title of the GUI (only works in 1.14+)
                .plugin(EscapeRooms.getPlugin())                                 //set the plugin instance
                .open(player);                                  //opens the GUI for the player provided
    }

    /**
     * Effect types to skip over when generating the potions
     * @param type - effect type
     * @return - whether this effect type should be allowed in the official list
     */
    private boolean isSkip(PotionEffectType type) {
        return type == PotionEffectType.INSTANT_DAMAGE ||
                type == PotionEffectType.INSTANT_HEALTH ||
                type == PotionEffectType.HERO_OF_THE_VILLAGE ||
                type == PotionEffectType.TRIAL_OMEN ||
                type == PotionEffectType.RAID_OMEN ||
                type == PotionEffectType.BAD_OMEN;
    }

    /**
     * Returns a color based on the potion effect type
     * @param type - potion effect type
     * @return - color that potion should be
     */
    private static Color getPotionColorForEffect(PotionEffectType type) {
        if (type == PotionEffectType.SPEED) {
            return Color.fromRGB(51, 235, 255); // Blue
        } else if (type == PotionEffectType.SLOWNESS) {
            return Color.fromRGB(139, 175, 224); // Dark Blue
        } else if (type == PotionEffectType.STRENGTH) {
            return Color.fromRGB(255, 199, 0); // Red
        } else if (type == PotionEffectType.WEAKNESS) {
            return Color.fromRGB(72, 77, 72); // Gray
        } else if (type == PotionEffectType.POISON) {
            return Color.fromRGB(135, 163, 99); // Green
        } else if (type == PotionEffectType.REGENERATION) {
            return Color.fromRGB(205, 92, 171); // Magenta
        } else if (type == PotionEffectType.FIRE_RESISTANCE) {
            return Color.fromRGB(255, 153, 0); // Orange
        } else if (type == PotionEffectType.NIGHT_VISION) {
            return Color.fromRGB(194, 255, 102); // Yellow
        } else if (type == PotionEffectType.INVISIBILITY) {
            return Color.fromRGB(246, 246, 246); // White
        } else if (type == PotionEffectType.JUMP_BOOST) {
            return Color.fromRGB(253, 255, 132); // Pale Yellow
        } else if (type == PotionEffectType.RESISTANCE) {
            return Color.fromRGB(0, 255, 255); // Cyan
        } else if (type == PotionEffectType.WATER_BREATHING) {
            return Color.fromRGB(152, 218, 192); // Sea foam green
        } else if (type == PotionEffectType.WITHER) {
            return Color.fromRGB(115, 97, 86); // dark brown
        } else if (type == PotionEffectType.SLOW_FALLING) {
            return Color.fromRGB(243, 207, 185);
        }else {
            return Color.fromRGB(255, 255, 255); // Default to White
        }
    }


    /**
     * Formats the effect name in a nicer way
     * @param type - potion effect type
     * @return - nicely formatted string for the potion effect type
     */
    private static String parseEffectName(PotionEffectType type) {
        String[] words = type.getKey().getKey().split("_");
        StringBuilder readableName = new StringBuilder();
        for (String word : words) {
            readableName.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1))
                    .append(" ");
        }
        return readableName.toString().trim();
    }
}
