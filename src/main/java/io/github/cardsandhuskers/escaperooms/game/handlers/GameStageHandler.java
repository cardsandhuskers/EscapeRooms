package io.github.cardsandhuskers.escaperooms.game.handlers;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.game.listeners.*;
import io.github.cardsandhuskers.escaperooms.game.objects.Countdown;
import io.github.cardsandhuskers.escaperooms.game.objects.GameMessages;
import io.github.cardsandhuskers.escaperooms.game.objects.Placeholder;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameStageHandler {
    List<TeamInstance> teamInstances;
    EscapeRooms plugin = EscapeRooms.getPlugin();
    List<Listener> listeners;

    private Countdown pregameTimer, gameTimer, gameEndTimer;

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
                    Placeholder.gameState = Placeholder.GameState.GAME_STARTING;


                },

                //Timer End
                () -> {
                    Placeholder.timeVar = 0;
                    startGame();


                },

                //Each Second
                (t) -> {
                    Placeholder.timeVar = t.getSecondsLeft();

                    if (t.getSecondsLeft() == t.getTotalSeconds() - 1) EscapeRooms.getPlugin().getServer().broadcast(GameMessages.getDescription(plugin.getConfig().getInt("courseLength")));
                    if (t.getSecondsLeft() == t.getTotalSeconds() - 11) EscapeRooms.getPlugin().getServer().broadcast(GameMessages.getPointsDescription());
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        pregameTimer.scheduleTimer();

    }

    public void startGame() {
        int gameTime = plugin.getConfig().getInt("gameTime");
        gameTimer = new Countdown(plugin,

                gameTime,
                //Timer Start
                () -> {
                    Placeholder.timeVar = gameTime;
            
                    Placeholder.gameState = Placeholder.GameState.GAME_ACTIVE;

                    //teleport players
                    for(Player p: Bukkit.getOnlinePlayers()) {
                        if (TeamHandler.getInstance().getPlayerTeam(p) == null) {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                    }

                    for(TeamInstance instance: teamInstances) {
                        instance.teleportToCurrentLevel();
                    }

                    for(TeamInstance instance: teamInstances) {
                        instance.executeLevelMechanics();
                    }

                    HashMap<Team, TeamInstance> teamInstanceMap = new HashMap<>();
                    for(TeamInstance t: teamInstances) {
                        teamInstanceMap.put(t.getTeam(), t);
                    }

                    listeners = registerListeners(teamInstanceMap);


                },

                //Timer End
                () -> {
                    Placeholder.timeVar = 0;



                },

                //Each Second
                (t) -> {
                    Placeholder.timeVar = t.getSecondsLeft();



                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameTimer.scheduleTimer();
    }

    public void startGameEnd() {
        int gameTime = plugin.getConfig().getInt("postgameTime");
        gameEndTimer = new Countdown(plugin,

                gameTime,
                //Timer Start
                () -> {
                    Placeholder.gameState = Placeholder.GameState.GAME_ACTIVE;


                },

                //Timer End
                () -> {
                    Placeholder.timeVar = 0;
                    endGame();


                },

                //Each Second
                (t) -> {
                    Placeholder.timeVar = t.getSecondsLeft();



                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameEndTimer.scheduleTimer();
    }

    public void endGame() {
        if(listeners != null) {
            for(Listener listener:listeners) {
                HandlerList.unregisterAll(listener);
            }
        }
    }

    private List<Listener> registerListeners(HashMap<Team, TeamInstance> teamInstanceMap) {
        List<Listener> listeners = new ArrayList<>();
        Listener playerInteractListener = new PlayerInteractListener(teamInstanceMap);
        listeners.add(playerInteractListener);
        Listener playerJoinListener = new PlayerJoinListener(teamInstanceMap);
        listeners.add(playerJoinListener);
        Listener playerMovementListener = new PlayerMovementListener(teamInstanceMap);
        listeners.add(playerMovementListener);
        Listener playerDeathListener = new PlayerRespawnListener(teamInstanceMap);
        listeners.add(playerDeathListener);
        Listener playerDamageListener = new PlayerDamageListener(teamInstanceMap);
        listeners.add(playerDamageListener);

        for(Listener l: listeners) {
            plugin.getServer().getPluginManager().registerEvents(l, plugin);
        }

        return listeners;
    }


}
