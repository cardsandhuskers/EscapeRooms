package io.github.cardsandhuskers.escaperooms.builder.mechanics.potionmechanic;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Small serializable class to store potion data (type, whether or not it's enabled, and duration and amplifier)
 * Duration is likely to be scrapped, all effects through this mechanic should be infinite
 */
public class PotionInfo implements ConfigurationSerializable {

    public int amplifier;
    public final PotionEffectType effectType;
    public boolean isEnabled;


    public PotionInfo(PotionEffectType effectType, int amplifier, boolean isEnabled) {
        this.effectType = effectType;
        this.amplifier = amplifier;
        this.isEnabled = isEnabled;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("type", effectType.getKey().toString());
        data.put("amplifier", amplifier);
        data.put("isEnabled", isEnabled);
        return data;
    }


    public static PotionInfo deserialize(Map<String, Object> data) {

        int amplifier = (int) data.get("amplifier");

        PotionEffectType type = PotionEffectType.getByKey(NamespacedKey.fromString((String) data.get("type")));
        boolean isEnabled = (boolean) data.get("isEnabled");
        return new PotionInfo(type, amplifier, isEnabled);
    }

}
