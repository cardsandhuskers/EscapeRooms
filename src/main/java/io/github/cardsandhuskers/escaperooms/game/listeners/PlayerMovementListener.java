package io.github.cardsandhuskers.escaperooms.game.listeners;

import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.Map;

/**
 * Listens for player movement event
 * @author jscotty
 */

public class PlayerMovementListener implements Listener {
    Map<Team, TeamInstance> teamInstances;

    public PlayerMovementListener(Map<Team, TeamInstance> teamInstances) {
         this.teamInstances = teamInstances;
    }

    /**
     * Listens for player movement and calls mechanic event
     * handler. Ignores player movement from players in
     * spectator.
     * @param e player movement event
     */
    @EventHandler
    public void onPlayerMovement(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if(p.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        Team team = TeamHandler.getInstance().getPlayerTeam(p);
        if (team != null) {
            TeamInstance teamInstance = teamInstances.get(team);

            if(teamInstance == null) return;

            List<Mechanic> mechanics = teamInstance.getCurrentLevel().getMechanics();
            for (Mechanic mechanic: mechanics) {
                mechanic.eventHandler(teamInstance, e);
            }
        }
    }
}
