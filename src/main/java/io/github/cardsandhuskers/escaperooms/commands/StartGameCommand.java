package io.github.cardsandhuskers.escaperooms.commands;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.game.handlers.WorldSetupHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartGameCommand implements CommandExecutor {
    EscapeRooms plugin = EscapeRooms.getPlugin();

    public StartGameCommand() {

    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player p) {
            if(p.isOp()) {
                startGame();
            } else {
                p.sendMessage(ChatColor.DARK_RED + "You must be an administrator to perform this command");
            }
        } else {
            startGame();
        }

        return true;
    }

    public void startGame() {
        WorldSetupHandler worldSetupHandler = new WorldSetupHandler();
        worldSetupHandler.setupWorld();
    }
}