package io.github.cardsandhuskers.escaperooms.builder.mechanics.button;

import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class BlockLocation implements ConfigurationSerializable {

    private final int x, y, z;
    private final BlockFace face;

    public BlockLocation(int x, int y, int z, BlockFace face) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public BlockFace getFace() { return face; }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("x", x);
        data.put("y", y);
        data.put("z", z);
        data.put("face", face.name()); // Save BlockFace as a string
        return data;
    }

    @Override
    public String toString() {
        return "BlockLocation{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", face=" + face +
                '}';
    }

    public static BlockLocation deserialize(Map<String, Object> data) {

        int x = (int) data.get("x");
        int y = (int) data.get("y");
        int z = (int) data.get("z");
        BlockFace face = BlockFace.valueOf((String) data.get("face"));
        return new BlockLocation(x, y, z, face);
    }
}
