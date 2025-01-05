package io.github.cardsandhuskers.escaperooms.builder.objects;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.StartingItemMechanic;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Level {
    private int lowerX, lowerY, lowerZ, higherX, higherY, higherZ;
    private Location pos1, pos2;
    private String name;
    private ArrayList<Mechanic> levelMechanics = new ArrayList<>();

    private GameMode gameMode = GameMode.ADVENTURE;

    private boolean envDamage = false;

    private boolean pvpDamage = false;
    private int minPlayers = 1;

    Vector spawnPointOffset;
    //TODO: add pitch and yaw

    public Level(String name) {
        this.name = name;
        writeData();
    }

    public String getName() {
        return name;
    }

    public Location getPos1() {
        return pos1;
    }
    public Location getPos2() {
        return pos2;
    }

    public void setPos1(Location pos) {
        pos1 = pos;
        writeData();

        if(pos2 != null) {
            assignCorners();
        }
    }

    public void setPos2(Location pos) {
        pos2 = pos;
        writeData();

        if(pos1 != null) {
            assignCorners();
        }
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
        writeData();
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setEnvDamage(boolean envDamage) {
        this.envDamage = envDamage;
        writeData();
    }

    public void setPvpDamage(boolean pvpDamage) {
        this.pvpDamage = pvpDamage;
        writeData();
    }
    public boolean isEnvDamage() {
        return envDamage;
    }

    public boolean isPvpDamage() {
        return pvpDamage;
    }

    public int getMinPlayers() {
        return  minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public void writeData() {
        EscapeRooms plugin = JavaPlugin.getPlugin(EscapeRooms.class);

        // Load the config file
        File file = new File(plugin.getDataFolder(), name + ".yml");

        // Create the file if it does not exist
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Load the configuration
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("levelName", name);
        config.set("pos1", pos1);
        config.set("pos2", pos2);
        config.set("gameMode", gameMode.name());
        config.set("envDamage", envDamage);
        config.set("pvpDamage", pvpDamage);
        config.set("minPlayers", minPlayers);

        if (spawnPointOffset != null) {
            config.set("spawnPoint.x", spawnPointOffset.getX());
            config.set("spawnPoint.y", spawnPointOffset.getY());
            config.set("spawnPoint.z", spawnPointOffset.getZ());
        }

        // Mechanics data saving logic
        List<Map<String, Object>> mechanicList = new ArrayList<>();

        for (Mechanic m : levelMechanics) {
            // For each mechanic, create the mechanic entry
            Map<String, Object> mechanicEntry = m.writeData();

            // Add the mechanic entry to the list
            mechanicList.add(mechanicEntry);
        }

        // Save the mechanics list to the config
        config.set("mechanics", mechanicList);
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Converts material to mechanic object
     * @param mat
     * @return
     */
    public Mechanic addMechanic(Material mat) {
        Mechanic mechanic = MechanicMapper.createTypedMechanic(mat, this);
        levelMechanics.add(mechanic);

        return mechanic;
    }

    public boolean setSpawnPoint(Location pos) {
        if(pos1 != null && pos2 != null) {
            int xDiff = (int) (pos.getX() - lowerX);
            int yDiff = (int) (pos.getY() - lowerY);
            int zDiff = (int) (pos.getZ() - lowerZ);
            spawnPointOffset = new Vector(xDiff, yDiff, zDiff);
            return true;
        } else return false;
    }

    public void setSpawnPointOffset(Vector offset) {
        spawnPointOffset = offset;
    }

    public Location getSpawnPoint() {
        if (spawnPointOffset != null && pos1 != null) return new Location(pos1.getWorld(), lowerX + spawnPointOffset.getX(), lowerY + spawnPointOffset.getY(), lowerZ + spawnPointOffset.getZ());
        else return null;
    }

    public List<Mechanic> getMechanics() {
        return new ArrayList<>(levelMechanics);
    }

    private void assignCorners() {
        lowerX = (int) Math.min(pos1.getX(), pos2.getX());
        higherX = (int) Math.max(pos1.getX(), pos2.getX());
        lowerY = (int) Math.min(pos1.getY(), pos2.getY());
        higherY = (int) Math.max(pos1.getY(), pos2.getY());
        lowerZ = (int) Math.min(pos1.getZ(), pos2.getZ());
        higherZ = (int) Math.max(pos1.getZ(), pos2.getZ());
    }

    public void addMechanic(Mechanic mechanic) {
        levelMechanics.add(mechanic);
    }
}