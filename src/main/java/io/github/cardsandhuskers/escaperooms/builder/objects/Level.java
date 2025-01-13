package io.github.cardsandhuskers.escaperooms.builder.objects;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    private Vector spawnPointOffset;
    private double spawnPitch = 0;

    private double spawnYaw = 0;

    public Level(String name) {
        this.name = name;
    }

    public boolean saveSchematic() {
        System.out.println("SAVING!");

        if (pos1 != null && pos2 != null) {
            EscapeRooms plugin = EscapeRooms.getPlugin();

            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                System.out.println("ASYNC!");

                try {
                    // Define the region
                    BlockVector3 bot = BlockVector3.at(lowerX, lowerY, lowerZ);
                    BlockVector3 top = BlockVector3.at(higherX, higherY, higherZ);
                    CuboidRegion region = new CuboidRegion(new BukkitWorld(pos1.getWorld()), bot, top);

                    System.out.println("Region: " + region);
                    System.out.println("Region Size: " + region.getVolume() + " blocks");

                    // Create the clipboard
                    BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
                    ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                            new BukkitWorld(pos1.getWorld()), region, clipboard, region.getMinimumPoint()
                    );
                    forwardExtentCopy.setCopyingEntities(true);

                    // Copy the blocks
                    Operations.complete(forwardExtentCopy);
                    System.out.println("Copied: " + clipboard.getRegion().getVolume() + " blocks to clipboard");

                    // Save to .schem file
                    File file = new File(plugin.getDataFolder(), getName() + ".schem");
                    try (ClipboardWriter writer = BuiltInClipboardFormat.FAST_V3.getWriter(new FileOutputStream(file))) {
                        writer.write(clipboard);
                        System.out.println("Schematic saved to: " + file.getAbsolutePath());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return true;
        }
        return false;
    }


    public void setPos1(Location pos) {
        pos1 = pos;

        if(pos2 != null) {
            assignCorners();
        }
    }

    public void setPos2(Location pos) {
        pos2 = pos;

        if(pos1 != null) {
            assignCorners();
        }
    }

    public double getSpawnPitch() {
        return spawnPitch;
    }
    public double getSpawnYaw() {
        return spawnYaw;
    }
    public void setSpawnPitch(double spawnPitch) {
        this.spawnPitch = spawnPitch;
    }
    public void setSpawnYaw(double spawnYaw) {
        this.spawnYaw = spawnYaw;
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
    public void setSpawnPointOffset(Vector offset) {
        spawnPointOffset = offset;
    }
    public List<Mechanic> getMechanics() {
        return new ArrayList<>(levelMechanics);
    }
    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
    public GameMode getGameMode() {
        return gameMode;
    }

    public void setEnvDamage(boolean envDamage) {
        this.envDamage = envDamage;
    }

    public void setPvpDamage(boolean pvpDamage) {
        this.pvpDamage = pvpDamage;
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
        System.out.println("WRITING LEVEL DATA");

        EscapeRooms plugin = EscapeRooms.getPlugin();

        List<String> levelNames = plugin.getConfig().getStringList("levels");
        if(! levelNames.contains(name)) levelNames.add(name);
        plugin.getConfig().set("levels", levelNames);
        plugin.saveConfig();

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
        config.set("spawnPoint.pitch", spawnPitch);
        config.set("spawnPoint.yaw", spawnYaw);

        // Mechanics data saving logic
        Map<String, Object> mechanicMap = new HashMap<>();

        for (Mechanic m : levelMechanics) {
            // For each mechanic, create the mechanic entry
            Map<String, Object> mechanicEntry = m.getData();

            // Add the mechanic entry to the list
            mechanicMap.put(String.valueOf(m.getID()), mechanicEntry);
        }

        // Save the mechanics list to the config
        config.set("mechanics", mechanicMap);
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
        if(mechanic != null) {
            levelMechanics.add(mechanic);
        }
        return mechanic;
    }

    public boolean setSpawnPoint(Location pos) {
        if(pos1 != null && pos2 != null) {
            int xDiff = (int) (pos.getX() - lowerX);
            int yDiff = (int) (pos.getY() - lowerY);
            int zDiff = (int) (pos.getZ() - lowerZ);
            spawnPointOffset = new Vector(xDiff, yDiff, zDiff);
            spawnPitch = pos.getPitch();
            spawnYaw = pos.getYaw();

            return true;
        } else return false;
    }

    public Location getSpawnPoint() {
        if (spawnPointOffset != null && pos1 != null) return new Location(pos1.getWorld(), lowerX + spawnPointOffset.getX(), lowerY + spawnPointOffset.getY(), lowerZ + spawnPointOffset.getZ());
        else return null;
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

    public Vector getDiff(Vector pos) {
        if(pos1 != null && pos2 != null) {
            int xDiff = (int) (pos.getX() - lowerX);
            int yDiff = (int) (pos.getY() - lowerY);
            int zDiff = (int) (pos.getZ() - lowerZ);
            return new Vector(xDiff, yDiff, zDiff);
        }
        return null;
    }

    public Vector getCoords(Vector offset) {
        if(pos1 != null && pos2 != null) {
            int x = (int) (offset.getX() + lowerX);
            int y = (int) (offset.getY() + lowerY);
            int z = (int) (offset.getZ() + lowerZ);
            return new Vector(x, y, z);
        }
        return null;
    }


}