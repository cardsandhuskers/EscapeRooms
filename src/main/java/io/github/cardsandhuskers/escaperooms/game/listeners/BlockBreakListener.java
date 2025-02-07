package io.github.cardsandhuskers.escaperooms.game.listeners;

import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.Map;

public class BlockBreakListener implements Listener {

    Map<Team, TeamInstance> teamInstances;

    public BlockBreakListener(Map<Team, TeamInstance> teamInstances) {
        this.teamInstances = teamInstances;

    }

    /**
     * Listens for players breaking a block and passes it to relevant mechanics.
     * @param e
     */
    @EventHandler
    public void onPlayerDamage(BlockBreakEvent e) {
        Player p = e.getPlayer();

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
