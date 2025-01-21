package io.github.cardsandhuskers.escaperooms.game.objects;

import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TeamInstance {

    Team team;
    HashMap<Level, Location> levelCorners;
    List<Level> levels;

    int currentLevel = 1;

    public TeamInstance(Team team, List<Level> levels, HashMap<Level, Location> levelCorners) {
        this.levels = levels;
        this.team = team;
        this.levelCorners = levelCorners;
    }

    public void teleportToCurrentLevel() {
        Level level = levels.get(currentLevel - 1);

        Location spawnPoint = level.calculateSpawnPoint(levelCorners.get(level));
        for(Player p:team.getOnlinePlayers()) {
            p.teleport(spawnPoint);
        }
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevel - 1);
    }



    public void onLevelFinish() {
        currentLevel++;
    }

}
