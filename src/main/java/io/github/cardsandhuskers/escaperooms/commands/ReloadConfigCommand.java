package io.github.cardsandhuskers.escaperooms.commands;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReloadConfigCommand implements CommandExecutor {
    public ReloadConfigCommand() {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        EscapeRooms plugin = EscapeRooms.getPlugin();

        if(commandSender instanceof Player p && p.isOp()) {
            plugin.reloadConfig();

            p.sendMessage(Component.text("Config Reloaded").color(NamedTextColor.GREEN));
        } else if(commandSender instanceof Player p) {
            p.sendMessage(Component.text("You don't have permissions").color(NamedTextColor.RED));
        } else {
            plugin.reloadConfig();
            plugin.getLogger().info("Config Reloaded");
        }
        return true;
    }
}
