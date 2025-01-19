package io.github.cardsandhuskers.escaperooms.commands;

import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EscapeRoomCommand implements CommandExecutor {
    EscapeRooms plugin = EscapeRooms.getPlugin();
    private EditorGUIHandler editorGUIHandler;

    public EscapeRoomCommand(EditorGUIHandler editorGUIHandler) {
        this. editorGUIHandler = editorGUIHandler;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player p) {
            if(p.isOp()) {
                editorGUIHandler.onMainGUIOpen(p);
            } else {
                p.sendMessage(ChatColor.DARK_RED + "You must be an administrator to perform this command");
            }
        } else {
            plugin.getLogger().warning("Cannot use this command from console.");
        }

        return true;
    }
}
