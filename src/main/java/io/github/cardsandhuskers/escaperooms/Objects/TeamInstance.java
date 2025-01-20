package io.github.cardsandhuskers.escaperooms.Objects;

import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.Location;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TeamInstance {

    Team team;
    HashMap<Level, Location> levelCorners;
    List<Level> levels;

    int currentLevel;

    public TeamInstance(Team team, List<Level> levels, HashMap<Level, Location> levelCorners) {
        this.levels = levels;
        this.team = team;
        this.levelCorners = levelCorners;
    }

}
