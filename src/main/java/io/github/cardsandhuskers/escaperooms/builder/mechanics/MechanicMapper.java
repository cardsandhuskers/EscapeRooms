package io.github.cardsandhuskers.escaperooms.builder.mechanics;

import io.github.cardsandhuskers.escaperooms.builder.mechanics.buttonmechanic.RandomButtonMechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.potionmechanic.PotionEffectsMechanic;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Handle any behavior that requires mapping mechanics, strings, material, classes
 */
public class MechanicMapper {

    /**
     * Add new mechanics to this list here
     * This is the ONLY place you need to do this
     */
    private static final ArrayList<MechanicDetails> mechanicDetails = new ArrayList<>(Arrays.asList(
            new MechanicDetails(StartingItemMechanic.class, "Give Item on Spawn", Material.BOOK),
            new MechanicDetails(RandomButtonMechanic.class, "Random Button Location", Material.STONE_BUTTON),
            new MechanicDetails(PotionEffectsMechanic.class, "Apply Potion Effects", Material.POTION),
            new MechanicDetails(ClearBlocksMechanic.class, "Clear Blocks", Material.STONE),
            new MechanicDetails(CheckpointMechanic.class, "Set Checkpoints", Material.RED_BED),
            new MechanicDetails(CustomDropMechanic.class, "Custom Item Drop", Material.DIAMOND_PICKAXE),
            new MechanicDetails(DisableInteractMechanic.class, "Disable Interact", Material.FURNACE),
            new MechanicDetails(SpecificBlockPlacementWinMechanic.class, "Specific Block Placements For Win", Material.BELL)
            ));

    /**
     * Subclass for mechanic details to hold the associations between classes, names, and identifier materials
     */
    public static class MechanicDetails {
        public final Class<?> classType;
        public final String name;
        public final Material mat;

        public MechanicDetails(Class<?> classType, String name, Material mat) {
            this.classType = classType;
            this.name = name;
            this.mat = mat;
        }
    }

    /**
     * Used to map typed mechanics correctly when loading them from a file
     *
     * @param ID - mechanic's UUID
     * @param type - mechanic type as a string
     * @param attributes - mechanic's attributes from config
     * @param level - level mechanic is tied to
     * @return - Instantiated mechanic object
     */
    public static Mechanic loadTypedMechanicFromFile(String ID, String type, ConfigurationSection attributes, Level level) {

        Mechanic mechanic = null;
        Class<?> clazz = getMechClass(type);

        try {
            mechanic = (Mechanic) clazz.getDeclaredConstructor(String.class, Level.class, ConfigurationSection.class).newInstance(ID, level, attributes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mechanic;
    }

    /**
     * Creates a typed mechanic based on the material clicked
     * @param mat - material clicked on
     * @param level - level mechanic should be tied to
     * @return - Instantiated mechanic object
     */
    public static Mechanic createTypedMechanic(Material mat, Level level) {
        Mechanic mechanic = null;
        Class<?> clazz = getMechClass(mat);

        try {
            mechanic = (Mechanic) clazz.getDeclaredConstructor(Level.class).newInstance(level);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mechanic;
    }


    /*
     * Series of simple getters for all combinations of associations in the MechanicDetails subclass
     *
     */
    public static Material getMechMaterial(Class<?> classType) {
        for(MechanicDetails details: mechanicDetails) {
            if(details.classType == classType) return details.mat;
        }
        return Material.AIR;
    }

    public static Material getMechMaterial(String name) {
        for(MechanicDetails details: mechanicDetails) {
            if(details.name.equals(name)) return details.mat;
        }
        return Material.AIR;
    }

    public static String getMechName(Class<?> classType) {
        for(MechanicDetails details: mechanicDetails) {
            if(details.classType == classType) return details.name;
        }
        return "EMPTY";
    }

    public static String getMechName(Material mat) {
        for(MechanicDetails details: mechanicDetails) {
            if(details.mat == mat) return details.name;
        }
        return "EMPTY";
    }

    public static Class<?> getMechClass(String name) {
        for(MechanicDetails details: mechanicDetails) {
            if(details.name.equals(name)) return details.classType;
        }
        return null;
    }

    public static Class<?> getMechClass(Material mat) {
        for(MechanicDetails details: mechanicDetails) {
            if(details.mat == mat) return details.classType;
        }
        return null;
    }

    public static boolean isValidMaterial(Material mat) {
        for(MechanicDetails details: mechanicDetails) {
            if(details.mat == mat) return true;
        }
        return false;
    }

    public static ArrayList<MechanicDetails> getMechanics() {
        return new ArrayList<>(mechanicDetails);
    }

}
