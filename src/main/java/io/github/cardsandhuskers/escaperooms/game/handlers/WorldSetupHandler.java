package io.github.cardsandhuskers.escaperooms.game.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;

public class WorldSetupHandler {

    private World world;
    private EscapeRooms plugin = EscapeRooms.getPlugin();

    public WorldSetupHandler() {
        world = Bukkit.getWorld(plugin.getConfig().getString("world"));
    }

    public void generateCourse() {
        List<Level> levels = LevelHandler.getInstance().getLevels();
        int numTeams = TeamHandler.getInstance().getTeams().size();

    }

}
