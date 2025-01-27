package io.github.cardsandhuskers.escaperooms.game.listeners;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.Map;

public class PlayerJoinListener implements Listener {

    Map<Team, TeamInstance> teamInstances;

    public PlayerJoinListener(Map<Team, TeamInstance> teamInstances) {
        this.teamInstances = teamInstances;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        Team team = TeamHandler.getInstance().getPlayerTeam(player);
        if(team != null) {
            TeamInstance teamInstance = teamInstances.get(team);
            teamInstance.teleportPlayer(player);

            List<Mechanic> mechanics = teamInstance.getCurrentLevel().getMechanics();
            for (Mechanic mechanic: mechanics) {
                mechanic.eventHandler(teamInstance, e);
            }
        } else {
            Location lobby = EscapeRooms.getPlugin().getConfig().getLocation("lobby");
            if(lobby != null) {
                player.teleport(lobby);

                EscapeRooms.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(EscapeRooms.getPlugin(), ()->{
                    player.setGameMode(GameMode.SPECTATOR);
                }, 20L);
            }


        }


    }
}
