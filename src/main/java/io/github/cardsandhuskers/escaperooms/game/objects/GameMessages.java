package io.github.cardsandhuskers.escaperooms.game.objects;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GameMessages {


    public static TextComponent getDescription(int num_puzzles) {
        return Component.text("----------------------------------------\n")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("          Temple Run          ")
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD)
                        .decoration(TextDecoration.STRIKETHROUGH, false))
                .append(Component.text("\nHow to Play:")
                        .color(NamedTextColor.DARK_AQUA)
                        .decorate(TextDecoration.BOLD))
                        .decoration(TextDecoration.STRIKETHROUGH, false)
                .append(Component.text("\nWork Together with your team to complete a series of ")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.STRIKETHROUGH, false)) // Reset strikethrough
                .append(Component.text(num_puzzles)
                        .color(NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD))
                        .decoration(TextDecoration.STRIKETHROUGH, false)
                .append(Component.text(" puzzles. Each puzzle will be unique," +
                                "\nrequiring collaboration and communication from your team to complete them in a timely manner." +
                                "\nComplete each puzzle before the other teams to score more points!")
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.STRIKETHROUGH, false)) // Reset strikethrough
                .append(Component.text("\n----------------------------------------")
                        .color(NamedTextColor.DARK_GREEN)
                        .decorate(TextDecoration.STRIKETHROUGH));
    }


    public static TextComponent getPointsDescription() {
        return Component.text("----------------------------------------")
                    .color(NamedTextColor.DARK_GREEN)
                    .decorate(TextDecoration.STRIKETHROUGH)
                .append(Component.text("\nHow The Game is Scored:"))
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.NOT_SET)
                .append(Component.text("\nFor Completing a Level: YOU GET POINTS!!!!!!"))
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.STRIKETHROUGH, TextDecoration.State.NOT_SET);
    }


    /**
     * Announces the top 5 earning players in the game
     */
    public static void announceTopPlayers() {
        Server server = EscapeRooms.getPlugin().getServer();
        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
        for(Team team: TeamHandler.getInstance().getTeams()) {
            for(Player p:team.getOnlinePlayers()) {
                tempPointsList.add(team.getPlayerTempPoints(p));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }

        tempPointsList.sort(Comparator.comparing(TempPointsHolder::getPoints));
        Collections.reverse(tempPointsList);

        int max;
        if(tempPointsList.size() >= 5) {
            max = 4;
        } else {
            max = tempPointsList.size() - 1;
        }

        server.broadcast(Component.text("Top 5 Players:").color(NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
        server.broadcast(Component.text("------------------------------").color(NamedTextColor.DARK_RED));

        int number = 1;
        for(int i = 0; i <= max; i++) {
            TempPointsHolder h = tempPointsList.get(i);

            TextComponent component = Component.text(number + ". " + TeamHandler.getInstance().getPlayerTeam(h.getPlayer()).color + h.getPlayer().getName());
            component = component.append(Component.text("    Points: " + h.getPoints()));
            server.broadcast(component);

            number++;
        }
        server.broadcast(Component.text("------------------------------").color(NamedTextColor.DARK_RED));
    }

    /**
     * Announces the leaderboard of teams based on points earned in the game
     */
    public static void announceTeamLeaderboard() {
        Server server = EscapeRooms.getPlugin().getServer();

        ArrayList<Team> teamList = TeamHandler.getInstance().getTeams();
        teamList.sort(Comparator.comparing(Team::getTempPoints));
        Collections.reverse(teamList);

        server.broadcast(Component.text("Team Leaderboard").color(NamedTextColor.BLUE).decoration(TextDecoration.BOLD, true));
        server.broadcast(Component.text("------------------------------").color(NamedTextColor.GREEN));

        int counter = 1;
        for(Team team:teamList) {

            TextComponent component = Component.text(counter + ". " + team.color);
            component = component.append(Component.text(team.getTeamName()).decoration(TextDecoration.BOLD, true));
            component = component.append(Component.text(" Points: " + team.getTempPoints()));
            server.broadcast(component);

            counter++;
        }

        server.broadcast(Component.text("------------------------------").color(NamedTextColor.GREEN));
        for(Player p: Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }
}
