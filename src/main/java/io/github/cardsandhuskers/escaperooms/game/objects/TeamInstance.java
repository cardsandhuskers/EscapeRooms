package io.github.cardsandhuskers.escaperooms.game.objects;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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

    HashMap<Level, Integer> levelStartTime, levelElapsedTime;

    int currentLevel = 1;

    public TeamInstance(Team team, List<Level> levels, HashMap<Level, Location> levelCorners) {
        this.levels = levels;
        this.team = team;
        this.levelCorners = levelCorners;

        Placeholder.currentLevelName.put(team, getCurrentLevel().getName());
        levelStartTime = new HashMap<>();
        levelElapsedTime = new HashMap<>();
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
        p.setGameMode(level.getGameMode());

        levelStartTime.put(level, Placeholder.timeVar);

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
        Level finishedLevel = getCurrentLevel();
        levelElapsedTime.put(finishedLevel, levelStartTime.get(finishedLevel) - Placeholder.timeVar);

        int time = levelElapsedTime.get(finishedLevel);
        int mins = time / 60;
        String seconds = String.format("%02d", time - (mins * 60));
        String timeString =  mins + ":" + seconds;

        for(Player p: Bukkit.getOnlinePlayers()) {
            Component teamMessage = Component.text("Congratulations! You finished level ").color(NamedTextColor.GREEN)
                    .append(Component.text(currentLevel)).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                    .append(Component.text(" in ")).color(NamedTextColor.GREEN)
                    .append(Component.text(timeString)).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                    .append(Component.text("! [+")).color(NamedTextColor.GREEN)
                    .append(Component.text(42)).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                    .append(Component.text("] points.")).color(NamedTextColor.GREEN);
            Component allMessage = Component.text(team.getTeamName() + " has completed level ").color(NamedTextColor.GRAY)
                    .append(Component.text(currentLevel).color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD))
                    .append(Component.text(" in ")).color(NamedTextColor.GRAY)
                    .append(Component.text(timeString).color(NamedTextColor.GRAY).decorate(TextDecoration.BOLD))
                    .append(Component.text(".")).color(NamedTextColor.GRAY);

            if(TeamHandler.getInstance().getPlayerTeam(p) == team) {

            } else {

            }

        }

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
