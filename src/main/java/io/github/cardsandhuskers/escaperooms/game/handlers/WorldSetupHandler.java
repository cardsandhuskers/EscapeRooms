package io.github.cardsandhuskers.escaperooms.game.handlers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.entity.EntityType;
import io.github.cardsandhuskers.escaperooms.EscapeRooms;
import io.github.cardsandhuskers.escaperooms.game.objects.TeamInstance;
import io.github.cardsandhuskers.escaperooms.builder.handlers.LevelHandler;
import io.github.cardsandhuskers.escaperooms.builder.objects.Level;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class WorldSetupHandler implements Listener {

    private World world;
    private EscapeRooms plugin = EscapeRooms.getPlugin();

    public WorldSetupHandler() {
        world = Bukkit.getWorld(plugin.getConfig().getString("world"));
    }


    public void setupWorld() {

        //register listener
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        //reset world
        CommandSender console = Bukkit.getConsoleSender();
        // Dispatch the command as the console
        Bukkit.dispatchCommand(console, "mvregen " + world.getName());

    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        if(e.getWorld() == world) {
            System.out.println("CORRECT WORLD LOADED");

            HandlerList.unregisterAll(this);

            List<Level> levelsToUse = selectLevels();
            List<TeamInstance> teamInstances = generateCourses(levelsToUse);

            GameStageHandler gameStageHandler = new GameStageHandler(teamInstances);
            gameStageHandler.startPregame();
        }
    }

    /**
     * Prunes levels that require more players than are available, then randomly removes levels until the length
     * matches the config set courseLength
     * @return levels to be used in the game
     */
    public List<Level> selectLevels() {
        int numLevels = plugin.getConfig().getInt("courseLength");
        List<Level> levels = LevelHandler.getInstance().getLevels();
        List<Level> usableLevels = new ArrayList<>(levels.size());

        int smallestTeamSize = 5;
        for(Team t: TeamHandler.getInstance().getTeams()) {
            smallestTeamSize = Math.min(smallestTeamSize, t.getSize());
        }

        for(Level level: levels) {
            if(level.getMinPlayers() <= smallestTeamSize) {
                usableLevels.add(level);
            }
        }

        while(usableLevels.size() > numLevels) {
            usableLevels.remove(new Random().nextInt(usableLevels.size()));
        }

        return usableLevels;

    }

    public List<TeamInstance> generateCourses(List<Level> levels) {

        int teamDist = plugin.getConfig().getInt("teamDistance");

        int x = 0,y = 0,z = 0;

        List<TeamInstance> teamInstances = new ArrayList<>();

        HashMap<Location, Level> allPastes = new HashMap<>();

        for (Team team : TeamHandler.getInstance().getTeams()) {
            HashMap<Level, Location> levelCorners = new HashMap<>();
            teamInstances.add(new TeamInstance(team, levels, levelCorners));
            z = 0;

            for (Level level : levels) {

                Location levelCorner = new Location(world, x, y, z);

                levelCorners.put(level, levelCorner);
                allPastes.put(levelCorner, level);

                z += level.getSize().getBlockZ() + 20;
            }

            x += teamDist;
        }

        for(Location l: allPastes.keySet()) {
            pasteSchematic(allPastes.get(l).getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
        }

        return teamInstances;
    }

    private void pasteSchematic(String levelName, int x, int y, int z) {

        //load level
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File file = new File(plugin.getDataFolder() + "/" + levelName + ".schem");
            ClipboardFormat format = ClipboardFormats.findByFile(file);

            //things in parentheses is a try with resources, it will auto close the reader at the end, like a 'finally' block
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                Clipboard clipboard = reader.read();

                try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(world)).build()) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(x, y, z))
                            .ignoreAirBlocks(false)
                            .copyEntities(true)
                            .copyBiomes(true)
                            .build();
                    Operations.complete(operation);

                    BlockVector3 dimensions = clipboard.getDimensions();
                    for (int bx = x; bx < dimensions.x() + x; bx++) {
                        for (int by = y; by < dimensions.y() + y; by++) {
                            for (int bz = z; bz < dimensions.z() + z; bz++) {
                                editSession.setBiome(bx, by, bz, BukkitAdapter.adapt(Biome.JUNGLE)); // Set the desired biome here
                            }
                        }
                    }

                    // Paste entities manually
                    for (com.sk89q.worldedit.entity.Entity entity : clipboard.getEntities()) {

                        // Check entity type
                        EntityType entityType = entity.getType();
                        org.bukkit.entity.EntityType bukkitEntityType = BukkitAdapter.adapt(entityType);

                        if (bukkitEntityType == org.bukkit.entity.EntityType.PAINTING || bukkitEntityType == org.bukkit.entity.EntityType.ITEM_FRAME) {

                            // Spawn entity
                            Bukkit.getScheduler().runTask(plugin, () ->{
                                com.sk89q.worldedit.util.Location entityLocation = entity.getLocation();
                                org.bukkit.util.Vector entityPos = new org.bukkit.util.Vector(entityLocation.x() + x, entityLocation.y() + y, entityLocation.z() + z);

                                // Create Bukkit location
                                Location spawnLocation = new Location(world, entityPos.getX(), entityPos.getY(), entityPos.getZ());
                                //spawn entity
                                org.bukkit.entity.Entity spawnedEntity = world.spawnEntity(spawnLocation, BukkitAdapter.adapt(entityType));

                                //get and apply item frame data
                                if (spawnedEntity instanceof ItemFrame itemFrame) {
                                    // Set the facing direction and item (if available)
                                    LinCompoundTag nbtData = entity.getState().getNbt();
                                    System.out.println("Item Frame NBT: " + nbtData);

                                    //get a BlockFace from the Facing byte
                                    LinTag<Byte> facingTag = nbtData.getTag("Facing", LinTagType.byteTag());
                                    BlockFace facing;
                                    switch(facingTag.value()) {
                                        case 0 -> facing = BlockFace.DOWN;
                                        case 1 -> facing = BlockFace.UP;
                                        case 2 -> facing = BlockFace.NORTH;
                                        case 3 -> facing = BlockFace.SOUTH;
                                        case 4 -> facing = BlockFace.WEST;
                                        case 5 -> facing = BlockFace.EAST;
                                        default -> throw new IllegalArgumentException("Invalid Facing Direction!");
                                    }
                                    itemFrame.setFacingDirection(facing, true);

                                    //if there is an item in the frame
                                    if (nbtData.findTag("Item", LinTagType.compoundTag()) != null) {
                                        LinCompoundTag itemTag = nbtData.getTag("Item", LinTagType.compoundTag());
                                        generateItemFromNBT(itemTag);

                                        System.out.println(itemTag);
                                        String type = itemTag.getTag("id", LinTagType.stringTag()).value();

                                        String[] parts = type.split(":", 2);
                                        NamespacedKey key = new NamespacedKey(parts[0], parts[1]);
                                        ItemType itemType = Registry.ITEM.get(key);

                                        itemFrame.setItem(itemType.createItemStack());
                                    }

                                } else if (spawnedEntity instanceof Painting painting) {
                                    // Set the facing direction and art type
                                    // Set the facing direction and item (if available)
                                    LinCompoundTag nbtData = entity.getState().getNbt();
                                    LinTag<Byte> facingTag = nbtData.findTag("facing", LinTagType.byteTag());
                                    BlockFace facing;
                                    switch(facingTag.value()) {
                                        case 0 -> facing = BlockFace.SOUTH;
                                        case 1 -> facing = BlockFace.WEST;
                                        case 2 -> facing = BlockFace.NORTH;
                                        case 3 -> facing = BlockFace.EAST;
                                        default -> facing = BlockFace.UP;
                                    }

                                    painting.setFacingDirection(facing, true);

                                    LinTag<String> variant = nbtData.getTag("variant", LinTagType.stringTag());
                                    String variantValue = variant.value();
                                    String[] parts = variantValue.split(":", 2);
                                    NamespacedKey key = new NamespacedKey(parts[0], parts[1]);
                                    Art art = Registry.ART.get(key); // This gets the Art variant

                                    painting.setArt(art, true);

                                }
                            });

                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private ItemStack generateItemFromNBT(LinCompoundTag itemTag) {
        String type = itemTag.getTag("id", LinTagType.stringTag()).value();
        int itemCount = itemTag.getTag("count", LinTagType.intTag()).value();

        //TODO need to pull data about items and place them

        String[] parts = type.split(":", 2);
        NamespacedKey key = new NamespacedKey(parts[0], parts[1]);
        ItemType itemType = Registry.ITEM.get(key);

        ItemStack stack = itemType.createItemStack(itemCount);

        return stack;
    }
}
