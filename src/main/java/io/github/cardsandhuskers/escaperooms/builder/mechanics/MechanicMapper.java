package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handle any behavior that requires mapping mechanics, strings, material, classes
 */
public class MechanicMapper {

    public static HashMap<String, Material> mechanicTypes = new HashMap<>(){{
        put("Block Break Tool", Material.DIAMOND_PICKAXE);
        put("Give Item on Spawn", Material.BOOK);
        put("Make Block Placeable", Material.IRON_BLOCK);
        put("Random Button Location", Material.STONE_BUTTON);
    }};

    public static HashMap<Class, Material> mechanicClassMap = new HashMap<>() {{
        put(StartingItemMechanic.class, Material.BOOK);
        put(RandomButtonMechanic.class, Material.STONE_BUTTON);
    }};


    public static Mechanic loadTypedMechanicFromFile(String ID, String type, ConfigurationSection attributes, Level level) {

        Mechanic mechanic = null;

        if(type.equals("Give Item on Spawn")) {
            // Create the Mechanic object and add it to the level
            String itemString = attributes.getString("item");
            ItemStack item = null;

            // Deserialize the item if it exists
            if (itemString != null && !itemString.isEmpty()) {
                item = Mechanic.deserializeItemStack(itemString);
            }

            mechanic = new StartingItemMechanic(ID, item, level);

        } else if (type.equals("Random Button Location")) {

            List<Map<?, ?>> locations = (List<Map<?, ?>>) attributes.getList("locations");
            List<BlockLocation> blockLocations = new ArrayList<>();

            if (locations != null) {
                for (Map<?, ?> locationData : locations) {
                    // Deserialize each location into a BlockLocation
                    blockLocations.add(BlockLocation.deserialize((Map<String, Object>) locationData));
                }
            }

            mechanic = new RandomButtonMechanic(ID, level, new ArrayList<>(blockLocations));

        }

        return mechanic;
    }

    public static Mechanic createTypedMechanic(Material mat, Level level) {
        Mechanic mechanic = null;
        switch (mat) {
            case BOOK -> {
                mechanic = new StartingItemMechanic(level);
            }
            case STONE_BUTTON -> {
                mechanic = new RandomButtonMechanic(level);
            }
        }
        return mechanic;
    }

    public static ItemStack createMechanicItem(Mechanic m, EscapeRooms plugin) {
        if(m == null) {
            return new ItemStack(Material.SPRUCE_BOAT);
        }

        ItemStack mechanicStack;
        List<Component> explanationLore;

        Material mat = mechanicClassMap.get(m.getClass());
        mechanicStack = new ItemStack(mat);

        //type StartingItem
        if(m instanceof StartingItemMechanic sim) {

            ItemStack item = sim.getItem();
            if(item!= null) explanationLore = List.of(Component.text("Current Item: " + item.getType().name()));
            else explanationLore = List.of(Component.text("Current Item: None"));

        } else if (m instanceof RandomButtonMechanic rbm) {
            explanationLore = List.of(Component.text("Sample Description Text"));
        }
        else {
            mechanicStack = new ItemStack(Material.OAK_BOAT);
            explanationLore = List.of(Component.text("Empty"));
        }
        ItemMeta mechanicMeta = mechanicStack.getItemMeta();

        //embed UUID
        NamespacedKey namespacedKey = new NamespacedKey(plugin, "ID");
        PersistentDataContainer container = mechanicMeta.getPersistentDataContainer();
        container.set(namespacedKey, PersistentDataType.STRING, m.getID().toString());
        //handle name and lore
        mechanicMeta.displayName(Component.text(MechanicMapper.getMechanicName(mechanicStack.getType())).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        mechanicMeta.lore(explanationLore);
        mechanicStack.setItemMeta(mechanicMeta);

        return mechanicStack;

    }


    /*
     * Do not need to be updated below here
     *
     */

    public static HashMap<String, Material> getMechanicTypes() {
        return new HashMap<>(mechanicTypes);
    }

    public static String getMechanicName(Material mat) {
        for (Map.Entry<String, Material> entry : mechanicTypes.entrySet()) {
            if (entry.getValue().equals(mat)) {
                return entry.getKey();
            }
        }
        return null; // Return null if no matching value is found
    }

    public static boolean isValidMaterial(Material mat) {
        return mechanicTypes.values().contains(mat);
    }

}
