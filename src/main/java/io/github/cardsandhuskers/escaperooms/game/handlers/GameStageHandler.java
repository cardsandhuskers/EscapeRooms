package io.github.cardsandhuskers.escaperooms.game.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.game.objects.Countdown;
import io.github.cardsandhuskers.escaperooms.game.objects.GameMessages;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.List;

public class GameStageHandler {
    List<TeamInstance> teamInstances;
    EscapeRooms plugin = EscapeRooms.getPlugin();

    private Countdown pregameTimer, gameTimer;

    public GameStageHandler(List<TeamInstance> teamInstances) {
        this.teamInstances = teamInstances;
    }

    //pregame
    public void startPregame() {

        int pregameTime = plugin.getConfig().getInt("pregameTime");
        pregameTimer = new Countdown(plugin,

                pregameTime,
                //Timer Start
                () -> {
                    Location lobby = plugin.getConfig().getLocation("lobby");
                    if(lobby != null) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.teleport(lobby);
                        }
                    } else {
                        plugin.getLogger().severe("No lobby location set!");
                    }


                },

                //Timer End
                () -> {
                    EscapeRooms.timeVar = 0;
                    startGame();


                },

                //Each Second
                (t) -> {
                    EscapeRooms.timeVar = t.getSecondsLeft();

                    if (t.getSecondsLeft() == t.getTotalSeconds() - 1) Bukkit.broadcastMessage(GameMessages.getDescription());

                    if (t.getSecondsLeft() == t.getTotalSeconds() - 11) Bukkit.broadcastMessage(GameMessages.getPointsDescription());
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        pregameTimer.scheduleTimer();



    }

    public void startGame() {
        //teleport players
        for(Player p: Bukkit.getOnlinePlayers()) {
            if (TeamHandler.getInstance().getPlayerTeam(p) == null) {
                p.setGameMode(GameMode.SPECTATOR);
            }
        }

        for(TeamInstance instance: teamInstances) {

        }


        int pregameTime = plugin.getConfig().getInt("pregameTime");
        gameTimer = new Countdown(plugin,

                pregameTime,
                //Timer Start
                () -> {
                    Location lobby = plugin.getConfig().getLocation("lobby");
                    if(lobby != null) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.teleport(lobby);
                        }
                    } else {
                        plugin.getLogger().severe("No lobby location set!");
                    }


                },

                //Timer End
                () -> {
                    EscapeRooms.timeVar = 0;



                },

                //Each Second
                (t) -> {
                    EscapeRooms.timeVar = t.getSecondsLeft();

                    if (t.getSecondsLeft() == t.getTotalSeconds() - 1) Bukkit.broadcastMessage(GameMessages.getDescription());

                    if (t.getSecondsLeft() == t.getTotalSeconds() - 11) Bukkit.broadcastMessage(GameMessages.getPointsDescription());
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameTimer.scheduleTimer();
    }


}
