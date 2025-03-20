package io.github.cardsandhuskers.escaperooms.builder.mechanics.checkplacedblocksmechanic;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

/**
 * Small serializable class that contains data for a block placement to check(x,y,z, and material)
 * Used for the CheckPlacedBlockMechanic
 */
public class BlockPlacementData implements ConfigurationSerializable {

    private final int x, y, z;
    private final Material material;

    public BlockPlacementData(int x, int y, int z, Material material) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.material = material;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public Material getMaterial() { return material; }

    public Vector getVector() {
        return new Vector(x,y,z);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("x", x);
        data.put("y", y);
        data.put("z", z);
        data.put("material", material.name()); // Save Material as a string
        return data;
    }

    @Override
    public String toString() {
        return "BlockLocation{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", material=" + material.name() +
                '}';
    }

    public static BlockPlacementData deserialize(Map<String, Object> data) {

        int x = (int) data.get("x");
        int y = (int) data.get("y");
        int z = (int) data.get("z");

        Material material = Material.valueOf((String) data.get("material"));
        return new BlockPlacementData(x, y, z, material);
    }
}
