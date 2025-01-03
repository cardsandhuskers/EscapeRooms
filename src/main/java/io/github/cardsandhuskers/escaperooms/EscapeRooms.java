package io.github.cardsandhuskers.escaperooms;

import io.github.cardsandhuskers.escaperooms.Objects.Placeholder;
import io.github.cardsandhuskers.escaperooms.builder.commands.EscapeRoomCommand;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.listeners.EditorGUIListener;
import io.github.cardsandhuskers.escaperooms.builder.listeners.InventoryCloseListener;
import io.github.cardsandhuskers.escaperooms.builder.listeners.PlayerClickListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class EscapeRooms extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            new Placeholder(this).register();

        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            System.out.println("Could not find PlaceholderAPI!");
            //Bukkit.getPluginManager().disablePlugin(this);
        }

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        LevelHandler.getInstance().loadLevels();

        EditorGUIHandler editorGUIHandler = new EditorGUIHandler(this);
        getCommand("escapeRoom").setExecutor(new EscapeRoomCommand(this, editorGUIHandler));
        getServer().getPluginManager().registerEvents(new EditorGUIListener(editorGUIHandler), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(editorGUIHandler), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
