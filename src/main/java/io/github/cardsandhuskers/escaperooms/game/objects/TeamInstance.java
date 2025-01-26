package io.github.cardsandhuskers.escaperooms.game.objects;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
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

        Placeholder.currentLevelName.put(team, getCurrentLevel().getName());
    }

    public void teleportToCurrentLevel() {
        Level level = levels.get(currentLevel - 1);

        Location spawnPoint = level.calculateSpawnPoint(levelCorners.get(level));
        for(Player p:team.getOnlinePlayers()) {
            p.teleport(spawnPoint);

            EscapeRooms.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(EscapeRooms.getPlugin(), ()->{
                p.setGameMode(level.getGameMode());
            }, 20L);
        }
    }

    public void teleportPlayer(Player p) {
        Level level = levels.get(currentLevel - 1);
        Location spawnPoint = level.calculateSpawnPoint(getCurrentLevelCorner());
        p.teleport(spawnPoint);

        EscapeRooms.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(EscapeRooms.getPlugin(), ()->{
            p.setGameMode(level.getGameMode());
        }, 20L);
    }

    public Level getCurrentLevel() {
        return levels.get(currentLevel - 1);
    }

    public void executeLevelMechanics() {

        for(Mechanic mechanic: getCurrentLevel().getMechanics()) {
            mechanic.levelStartExecution(this);
        }
    }

    public Location getCurrentLevelCorner() {
        return levelCorners.get(levels.get(currentLevel - 1)).clone();
    }

    public Team getTeam() {
        return team;
    }

    public boolean isLevelEndPressed(PlayerInteractEvent e) {
        if(e.getClickedBlock() == null) return false;

        Location loc = e.getClickedBlock().getLocation();
        Location corner = getCurrentLevelCorner();

        if (getCurrentLevel().isEndButton(corner, loc)) {
            startNextLevel();
            return true;
        }

        return false;
    }

    public void startNextLevel() {

        if(currentLevel == levels.size()) {
            //GAME OVER
            for(Player p: team.getOnlinePlayers()) {
                p.setGameMode(GameMode.SPECTATOR);
            }

            return;
        }

        currentLevel++;

        Placeholder.currentLevelCount.put(team, currentLevel);
        Placeholder.currentLevelName.put(team, getCurrentLevel().getName());
        teleportToCurrentLevel();
        executeLevelMechanics();
    }

}
