package io.github.cardsandhuskers.escaperooms;

import io.github.cardsandhuskers.escaperooms.Objects.Placeholder;
import io.github.cardsandhuskers.escaperooms.commands.EscapeRoomCommand;
import io.github.cardsandhuskers.escaperooms.builder.handlers.EditorGUIHandler;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.listeners.EditorGUIListener;
import io.github.cardsandhuskers.escaperooms.builder.listeners.InventoryCloseListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class EscapeRooms extends JavaPlugin {

    private static EscapeRooms plugin;

    @Override
    public void onEnable() {
        plugin = this;

        // Plugin startup logic
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            new Placeholder().register();

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

        EditorGUIHandler editorGUIHandler = new EditorGUIHandler();
        getCommand("escapeRoom").setExecutor(new EscapeRoomCommand(editorGUIHandler));
        getServer().getPluginManager().registerEvents(new EditorGUIListener(editorGUIHandler), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(editorGUIHandler), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static EscapeRooms getInstance() {
        return plugin;
    }
}
