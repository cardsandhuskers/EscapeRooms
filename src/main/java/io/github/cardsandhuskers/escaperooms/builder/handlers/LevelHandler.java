package io.github.cardsandhuskers.escaperooms.builder.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class LevelHandler {

    private static LevelHandler levelHandler;
    private final ArrayList<Level> levels = new ArrayList<>();

    private LevelHandler() {

    }

    /**
     * Gets the instance of this Singleton class
     */
    public static LevelHandler getInstance() {
        if (levelHandler == null) {
            levelHandler = new LevelHandler();
        }

        return levelHandler;
    }


    /**
     * Creates a new Level object and adds it to the level list
     * @param levelName - name of level being created
     * @param p - player making the level
     */
    public void createLevel(Player p, String levelName) {
        for (Level level: levels) {
            if (level.getName().equals(levelName)) {
                p.sendMessage(Component.text("Level by this name already exists").color(NamedTextColor.RED));
                return;
            }
        }

        Level level = new Level(levelName);
        levels.add(level);
        p.sendMessage(Component.text("Level created successfully").color(NamedTextColor.GREEN));
        level.writeData();

    }

    /**
     * Loads all levels from config files
     */
    public void loadLevels() {
        EscapeRooms plugin = EscapeRooms.getPlugin();

        List<String> levelNames = plugin.getConfig().getStringList("levels");

        for (String levelName : levelNames) {
            System.out.println("TEST: " + levelName);
            try {
                File file = new File(plugin.getDataFolder(), levelName + ".yml");
                if (file.exists()) {
                    FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                    Location pos1 = config.getLocation("pos1");
                    Location pos2 = config.getLocation("pos2");
                    GameMode gameMode = GameMode.valueOf(config.getString("gameMode"));
                    boolean envDamage = config.getBoolean("envDamage");
                    boolean pvpDamage = config.getBoolean("pvpDamage");
                    int minPlayers = config.getInt("minPlayers");

                    Level level = new Level(levelName);
                    if (pos1 != null) level.setPos1(pos1);
                    if (pos2 != null) level.setPos2(pos2);
                    level.setEnvDamage(envDamage);
                    level.setPvpDamage(pvpDamage);
                    level.setGameMode(gameMode);
                    level.setMinPlayers(minPlayers);

                    //spawn point
                    try {
                        double x = config.getDouble("spawnPoint.x");
                        double y = config.getDouble("spawnPoint.y");
                        double z = config.getDouble("spawnPoint.z");

                        double pitch = config.getDouble("spawnPoint.pitch");
                        double yaw = config.getDouble("spawnPoint.yaw");

                        Vector spawnOffset = new Vector(x, y, z);
                        level.setSpawnPointOffset(spawnOffset);
                        level.setSpawnPitch(pitch);
                        level.setSpawnYaw(yaw);
                    } catch (Exception e) {
                        // Handle missing or corrupted spawn point data
                        e.printStackTrace();
                    }

                    //level end button
                    try {
                        double x = config.getDouble("levelEnd.x");
                        double y = config.getDouble("levelEnd.y");
                        double z = config.getDouble("levelEnd.z");

                        Vector levelEndOffset = new Vector(x, y, z);
                        level.setLevelEndButtonOffset(levelEndOffset);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                    // Load mechanics using getConfigurationSection
                    ConfigurationSection mechanicSection = config.getConfigurationSection("mechanics");
                    if (mechanicSection != null) {
                        for (String mechanicID : mechanicSection.getKeys(false)) {  // Get all mechanic IDs
                            ConfigurationSection attributesSection = mechanicSection.getConfigurationSection(mechanicID);

                            if (attributesSection != null) {
                                String type = attributesSection.getString("type");

                                // Use the attributesSection to retrieve other properties, like locations, etc.
                                Mechanic mechanic = MechanicMapper.loadTypedMechanicFromFile(mechanicID, type, attributesSection, level);
                                level.addMechanic(mechanic); // Add mechanic to the level
                            }
                        }
                    }

                    levels.add(level);
                }
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                String stackTraceString = sw.toString();
                plugin.getLogger().severe("Error loading level: " + levelName + "\n" + stackTraceString);
            }
        }
    }

    /**
     * Deletes a level from the list and the configs
     * @param level - level to delete
     */
    public void deleteLevel(Level level) {
        EscapeRooms plugin = EscapeRooms.getPlugin();

        File file = new File(plugin.getDataFolder(), level.getName() + ".yml");
        file.delete();

        FileConfiguration config = plugin.getConfig();
        List<String> configLevels = config.getStringList("levels");

        // Remove the item (e.g., "asdf")
        if (configLevels.contains(level.getName())) {
            configLevels.remove(level.getName());
            config.set("levels", configLevels); // Update the list in the config
            plugin.saveConfig(); // Save the changes to the config.yml file
            plugin.getLogger().info( level.getName() + " has been removed from the levels list.");
        }

        levels.remove(level);
    }

    /**
     * Deletes a level from the list and the configs
     * @param levelName
     */
    public void deleteLevel(String levelName) {
        deleteLevel(getLevel(levelName));
    }

    /**
     * Gets a Level object based on the name
     * @param name - name of level to look for
     * @return - level object of that level
     */
    public Level getLevel(String name) {
        for (Level level: levels) {
            if(level.getName().equals(name)) {
                return level;
            }
        }
        return null;
    }

    /**
     * Sets a level's corner coordinates
     * @param pos - location to set a corner at
     * @param level - level to set the corner for
     * @param mat - which wand was used (wand for pos1 or pos2 corner)
     * @return - whether it was successful
     */
    public boolean setLevelPos(Location pos, Level level, Material mat) {
        if(mat == Material.BREEZE_ROD) {
            level.setPos1(pos);
        } else if (mat == Material.BLAZE_ROD) {
            level.setPos2(pos);
        }

        return true;
    }

    public ArrayList<Level> getLevels() {
        return new ArrayList<>(levels);
    }
}
