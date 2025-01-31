package io.github.cardsandhuskers.escaperooms.game.listeners;

import io.github.cardsandhuskers.escaperooms.builder.mechanics.Mechanic;
import io.github.cardsandhuskers.escaperooms.game.objects.Placeholder;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Map;

public class PlayerDamageListener implements Listener {

    Map<Team, TeamInstance> teamInstances;

    public PlayerDamageListener(Map<Team, TeamInstance> teamInstances) {
        this.teamInstances = teamInstances;

    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {

        if (e.getEntity().getType() == EntityType.PLAYER) {
            Player p = (Player) e.getEntity();
            if(Placeholder.gameState == Placeholder.GameState.GAME_STARTING) e.setCancelled(true);

            Team team = TeamHandler.getInstance().getPlayerTeam(p);
            if (team != null) {
                TeamInstance teamInstance = teamInstances.get(team);
                if(teamInstance == null) return;

                //cancel damage if damage type is set to off in the level settings
                if(e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                    EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
                    if(event.getDamager().getType() == EntityType.PLAYER) {
                        if (!teamInstance.getCurrentLevel().isPvpDamage()) e.setCancelled(true);
                    } else {
                        if(!teamInstance.getCurrentLevel().isEnvDamage()) e.setCancelled(true);
                    }
                } else {
                    if(!teamInstance.getCurrentLevel().isEnvDamage()) e.setCancelled(true);
                }


                List<Mechanic> mechanics = teamInstance.getCurrentLevel().getMechanics();
                for (Mechanic mechanic : mechanics) {
                    mechanic.eventHandler(teamInstance, e);
                }
            }
        }
    }


}
