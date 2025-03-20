package io.github.cardsandhuskers.escaperooms.game.objects;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        levelStartTime.put(level, Placeholder.timeVar);

        Location spawnPoint = level.calculateSpawnPoint(levelCorners.get(level));
        for(Player p:team.getOnlinePlayers()) {
            p.getInventory().clear();
            p.setLevel(0);
            p.setExp(0);
            p.setHealth(20);
            p.setSaturation(20);
            p.clearActivePotionEffects();

            p.teleport(spawnPoint);
            p.setGameMode(level.getGameMode());
        }
    }

    public void teleportPlayer(Player p) {
        Level level = levels.get(currentLevel - 1);
        Location spawnPoint = level.calculateSpawnPoint(getCurrentLevelCorner());
        p.teleport(spawnPoint);
        p.setGameMode(level.getGameMode());

    }

    public Level getCurrentLevel() {
        return levels.get(currentLevel - 1);
    }

    public void executeLevelMechanics() {

        for(Mechanic mechanic: getCurrentLevel().getMechanics()) {
            mechanic.levelStartExecution(this);
        }
    }

    /**
     * Returns a copy of the level corner (lowest x,y,z) of the level that the team is currently on.
     * @return a copy of the corner
     */
    public Location getCurrentLevelCorner() {
        return levelCorners.get(levels.get(currentLevel - 1)).clone();
    }

    public Team getTeam() {
        return team;
    }

    public boolean isLevelEndPressed(PlayerInteractEvent e, Map<Team, TeamInstance> teamInstances) {
        if(e.getClickedBlock() == null) return false;

        Location loc = e.getClickedBlock().getLocation();
        Location corner = getCurrentLevelCorner();

        if (getCurrentLevel().isEndButton(corner, loc)) {
            levelEndMessages(teamInstances);
            startNextLevel();
            return true;
        }

        return false;
    }

    public void levelEndMessages(Map<Team, TeamInstance> teamInstanceMap) {

        int teamsDone = 0;
        for(TeamInstance teamInstance: teamInstanceMap.values()) {
            if(teamInstance == this) continue;
            if (teamInstance.hasCompletedLevel(currentLevel)) teamsDone++;
        }

        EscapeRooms plugin = EscapeRooms.getPlugin();
        double maxPoints = plugin.getConfig().getDouble("maxPoints");
        double dropoff = plugin.getConfig().getDouble("dropoff");

        double points = maxPoints - (teamsDone * dropoff);
        double playerPoints = points / team.getSize();

        Level finishedLevel = getCurrentLevel();
        levelElapsedTime.put(finishedLevel, levelStartTime.get(finishedLevel) - Placeholder.timeVar);

        int time = levelElapsedTime.get(finishedLevel);
        int mins = time / 60;
        String seconds = String.format("%02d", time - (mins * 60));
        String timeString =  mins + ":" + seconds;


        String placeString;
        if (teamsDone == 0) placeString = "st";
        else if(teamsDone == 1) placeString = "nd";
        else if(teamsDone == 2) placeString = "rd";
        else placeString = "th";

        Component teamMessage = Component.text("Congratulations! You finished level ").color(NamedTextColor.GREEN)
                .append(Component.text(currentLevel).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(" in ").color(NamedTextColor.GREEN))
                //place
                .append(Component.text(teamsDone + 1).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(placeString).color(NamedTextColor.GREEN))
                .append(Component.text(" place!").color(NamedTextColor.GREEN))

                //time
                .append(Component.text(" Time: ("))
                .append(Component.text(timeString).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text(")"))

                //points
                .append(Component.text(" [+"))
                .append(Component.text(playerPoints).color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text("] points."));

        Component allMessage = Component.text(team.getColor() + team.getTeamName())
                .append(Component.text(" has completed level ").color(NamedTextColor.GRAY))
                .append(Component.text(currentLevel).color(NamedTextColor.YELLOW))
                .append(Component.text(" in ").color(NamedTextColor.GRAY))
                //place
                .append(Component.text(teamsDone + 1).color(NamedTextColor.YELLOW))
                .append(Component.text(placeString + " place.").color(NamedTextColor.GRAY))

                //time
                .append(Component.text(" Time: (").color(NamedTextColor.GRAY))
                .append(Component.text(timeString).color(NamedTextColor.YELLOW))
                .append(Component.text(")").color(NamedTextColor.GRAY));


        for(Player p: Bukkit.getOnlinePlayers()) {
            if(TeamHandler.getInstance().getPlayerTeam(p) == team) {
                p.sendMessage(teamMessage);
                Sound sound = Sound.sound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, Sound.Source.PLAYER, 1f, 1f);
                p.playSound(sound, Sound.Emitter.self());

                team.addTempPoints(p, playerPoints);
            } else {
                p.sendMessage(allMessage);
            }
        }
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

    public Location getLocationFromOffset(Vector offset) {
        return getCurrentLevelCorner().add(offset);
    }

    public boolean hasCompletedLevel(int testLevel) {
        return currentLevel > testLevel;
    }

}
