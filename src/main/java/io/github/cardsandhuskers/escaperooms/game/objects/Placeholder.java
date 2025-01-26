package io.github.cardsandhuskers.escaperooms.game.objects;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class Placeholder extends PlaceholderExpansion{
    private final EscapeRooms plugin = EscapeRooms.getPlugin();
    public static int timeVar = 0;
    public static GameState gameState = GameState.GAME_STARTING;
    public static HashMap<Team, Integer> currentLevelCount = new HashMap<>();
    public static HashMap<Team, String> currentLevelName = new HashMap<>();


    public Placeholder() {

    }
    @Override
    public String getIdentifier() {
        return "escaperooms";
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
        if(s.equalsIgnoreCase("timerstage")) {

            switch (gameState) {
                case GAME_STARTING -> {
                    return "Game Starts";
                }
                case GAME_ACTIVE -> {
                    return "Game Ends";
                }
                case GAME_OVER -> {
                    return "Return to Lobby";
                }
            }

        } else if (s.equalsIgnoreCase("timer")) {
            int mins = timeVar / 60;
            String seconds = String.format("%02d", timeVar - (mins * 60));
            return mins + ":" + seconds;
        } else if (s.equalsIgnoreCase("level")) {
            Team team = TeamHandler.getInstance().getPlayerTeam((Player)p);
            if (team != null) {
                return currentLevelCount.getOrDefault(team, 1) + "";
            } else {
                return "X";
            }
        } else if (s.equalsIgnoreCase("totallevels")) {
            return EscapeRooms.getPlugin().getConfig().getInt("courseLength") + "";
        } else if (s.equalsIgnoreCase("levelname")) {
            Team team = TeamHandler.getInstance().getPlayerTeam((Player)p);
            if (team != null) {
                return currentLevelName.getOrDefault(team, "NONE");

            } else {
                return "No Level";
            }

        }


        return "";
    }

    public enum GameState {
        GAME_STARTING,
        GAME_ACTIVE,
        GAME_OVER
    }

}
