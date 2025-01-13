package io.github.cardsandhuskers.escaperooms.Objects;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class Placeholder extends PlaceholderExpansion{
    private final EscapeRooms plugin = EscapeRooms.getPlugin();

    public Placeholder() {

    }
    @Override
    public String getIdentifier() {
        return "Teams";
    }
    @Override
    public String getAuthor() {
        return "cardsandhuskers";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer p, String s) {
        return "";
    }

}
