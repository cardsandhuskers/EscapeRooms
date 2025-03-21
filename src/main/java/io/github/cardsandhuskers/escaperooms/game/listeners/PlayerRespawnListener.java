package io.github.cardsandhuskers.escaperooms.game.listeners;

import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.Map;

/**
 * Player Respawen listener
 * @author jscotty
 */
public class PlayerRespawnListener implements Listener {
    Map<Team, TeamInstance> teamInstances;

    public PlayerRespawnListener(Map<Team, TeamInstance> teamInstances) {
        this.teamInstances = teamInstances;
    }

    /**
     * Calls handleEvent for mechanic upon player respawn
     * @param e player respawn
     */
    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent e) {
        Team team = TeamHandler.getInstance().getPlayerTeam(e.getPlayer());
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
