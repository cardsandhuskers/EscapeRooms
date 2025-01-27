package io.github.cardsandhuskers.escaperooms.commands;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetLobbyCommand implements CommandExecutor {

    public SetLobbyCommand() {

    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof  Player p && p.isOp()) {
            EscapeRooms plugin = EscapeRooms.getPlugin();
            Location l = p.getLocation();
            plugin.getConfig().set("lobby", l);
            plugin.saveConfig();
            p.sendMessage("Location set to " + l.toString());


        } else if(sender instanceof Player p) {
            p.sendMessage(Component.text("ERROR: You do not have sufficient permission to do this").color(NamedTextColor.RED));
        } else {
            System.out.println(ChatColor.RED + "ERROR: Cannot run from console");
        }
        return true;
    }
}
