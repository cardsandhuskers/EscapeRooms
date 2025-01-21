package io.github.cardsandhuskers.escaperooms.game.listeners;

import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.builder.mechanics.MechanicMapper;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

public class PlayerInteractListener implements Listener {
    Map<Team, TeamInstance> teamInstances;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {

        Team team = TeamHandler.getInstance().getPlayerTeam(e.getPlayer());
        if (team != null) {

            TeamInstance teamInstance = teamInstances.get(team);
            List<Mechanic> mechanics = teamInstance.getCurrentLevel().getMechanics();
            for (Mechanic mechanic: mechanics) {
                mechanic.eventHandler(teamInstance, e);
            }
        }




    }
}
