package io.github.cardsandhuskers.escaperooms.builder.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.StartingItemMechanic;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelHandler {

    private static LevelHandler levelHandler;
    private ArrayList<Level> levels = new ArrayList<>();

    private LevelHandler() {

    }

    public static LevelHandler getInstance() {
        if (levelHandler == null) {
            levelHandler = new LevelHandler();
        }

        return levelHandler;
    }


    /**
     * Creates a new Map object
     * @param levelName
     * @return - is creation successful
     */
    public boolean createLevel(Player p, String levelName) {
        for (Level level: levels) {
            if (level.getName().equals(levelName)) {
                p.sendMessage(Component.text("Level by this name already exists").color(NamedTextColor.RED));
                return false;
            }
        }

        Level level = new Level(levelName);
        levels.add(level);
        p.sendMessage(Component.text("Level created successfully").color(NamedTextColor.GREEN));
        level.writeData();

        return true;
    }

    public void loadLevels() {
        EscapeRooms plugin = JavaPlugin.getPlugin(EscapeRooms.class);

        List<String> levelNames = plugin.getConfig().getStringList("levels");

        for (String levelName : levelNames) {
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
                    }

                    // Load mechanics
                    List<Map<?, ?>> mechanicList = (List<Map<?, ?>>) config.getList("mechanics");
                    if (mechanicList != null) {
                        for (Map<?, ?> mechanicData : mechanicList) {
                            for (Map.Entry<?, ?> entry : mechanicData.entrySet()) {
                                String mechanicID = entry.getKey().toString();
                                Map<?, ?> attributes = (Map<?, ?>) entry.getValue();

                                String type = (String) attributes.get("type");

                                Mechanic mechanic = MechanicMapper.createTypedMechanic(mechanicID, type, attributes, level);
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


    public Level getLevel(String name) {
        for (Level level: levels) {
            if(level.getName().equals(name)) {
                return level;
            }
        }
        return null;
    }

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
