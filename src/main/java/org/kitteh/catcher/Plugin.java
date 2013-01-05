package org.kitteh.catcher;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.minecraft.server.v1_4_6.ChunkProviderServer;
import net.minecraft.server.v1_4_6.Entity;
import net.minecraft.server.v1_4_6.EntityHuman;
import net.minecraft.server.v1_4_6.EntityTracker;
import net.minecraft.server.v1_4_6.EntityTrackerEntry;
import net.minecraft.server.v1_4_6.TileEntity;
import net.minecraft.server.v1_4_6.World;
import net.minecraft.server.v1_4_6.WorldServer;

import org.bukkit.craftbukkit.v1_4_6.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

@SuppressWarnings("unchecked")
public class Plugin extends JavaPlugin {

    public enum Badness {
        VERY_BAD,
        RISKY;
    }

    private class Frmttr extends java.util.logging.Formatter {
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public String format(LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            builder.append(this.date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(this.formatMessage(record));
            builder.append('\n');
            final Throwable ex = record.getThrown();
            if (ex != null) {
                ex.setStackTrace(Arrays.copyOfRange(ex.getStackTrace(), 1, ex.getStackTrace().length));
                final StringWriter writer = new StringWriter();
                ex.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }

    private class Output implements Runnable {

        private Map<String, PluginClassLoader> loaders;

        @Override
        public void run() {
            try {
                this.loaders = (Map<String, PluginClassLoader>) Plugin.this.jplLoaders.get(Plugin.this.getPluginLoader());
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
                return;
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
                return;
            }
            boolean found = false;
            while (!Plugin.this.badList.isEmpty()) {
                found = true;
                this.log(Plugin.this.badList.remove(0), "dangerous");
            }
            if (!Plugin.this.onlyDangerous) {
                while (!Plugin.this.riskyList.isEmpty()) {
                    found = true;
                    this.log(Plugin.this.riskyList.remove(0), "risky");
                }
            }
            if (found) {
                Plugin.this.getLogger().severe("Found an async call. Check " + Plugin.this.asyncLogFile);
            }
        }

        private void log(Throwable t, String desc) {
            final Set<String> set = new HashSet<String>();
            for (final String plugin : this.loaders.keySet()) {
                if (plugin.equals(Plugin.this.thisName)) {
                    continue;
                }
                final PluginClassLoader loader = this.loaders.get(plugin);
                Map<String, Class<?>> classes;
                try {
                    classes = (Map<String, Class<?>>) Plugin.this.pclClasses.get(loader);
                } catch (final Exception e) {
                    return;
                }
                for (final StackTraceElement e : t.getStackTrace()) {
                    if (classes.containsKey(e.getClassName())) {
                        set.add(plugin);
                    }
                }
            }
            String message;
            if (set.isEmpty()) {
                message = "Found an async call I can't trace";
            } else {
                final StringBuilder builder = new StringBuilder();
                builder.append("Found " + desc + " async call. Might be from: ");
                for (final String plugin : set) {
                    final org.bukkit.plugin.Plugin p = Plugin.this.getServer().getPluginManager().getPlugin(plugin);
                    builder.append(plugin).append(" ").append(p.getDescription().getVersion());
                    builder.append(" (");
                    for (final String author : p.getDescription().getAuthors()) {
                        builder.append(author).append(" ");
                    }
                    builder.append(") ");
                }
                message = builder.toString();
            }
            Plugin.this.logger.log(Level.WARNING, message, t);
        }
    }

    private String thisName;
    private Field jplLoaders;
    private Field pclClasses;
    private Logger logger;
    private File asyncLogFile;
    private final List<Throwable> riskyList = Collections.synchronizedList(new ArrayList<Throwable>());
    private final List<Throwable> badList = Collections.synchronizedList(new ArrayList<Throwable>());
    private boolean onlyDangerous;

    public void add(Throwable throwable, Badness badness) {
        switch (badness) {
            case VERY_BAD:
                this.badList.add(throwable);
                break;
            default:
                this.riskyList.add(throwable);
                break;
        }
    }

    @Override
    public void onDisable() {
        try {
            final Field fieldEntities = World.class.getDeclaredField("entityList");
            fieldEntities.setAccessible(true);
            final Field fieldPlayers = World.class.getDeclaredField("players");
            fieldPlayers.setAccessible(true);
            final Field fieldTileEntities = World.class.getDeclaredField("tileEntityList");
            fieldTileEntities.setAccessible(true);
            final Field fieldUnloadQueue = ChunkProviderServer.class.getDeclaredField("unloadQueue");
            fieldUnloadQueue.setAccessible(true);
            final Field fieldEntityTrackerSet = EntityTracker.class.getDeclaredField("b");
            fieldEntityTrackerSet.setAccessible(true);
            for (final org.bukkit.World bworld : this.getServer().getWorlds()) {
                final World world = ((CraftWorld) bworld).getHandle();
                fieldEntities.set(world, new ArrayList<Entity>((List<Entity>) fieldEntities.get(world)));
                fieldPlayers.set(world, new ArrayList<EntityHuman>((List<EntityHuman>) fieldPlayers.get(world)));
                fieldTileEntities.set(world, new ArrayList<TileEntity>((List<TileEntity>) fieldTileEntities.get(world)));
                final EntityTracker tracker = ((WorldServer) world).tracker;
                fieldEntityTrackerSet.set(tracker, new HashSet<EntityTrackerEntry>((Set<EntityTrackerEntry>) fieldEntityTrackerSet.get(tracker)));
            }
        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to disable properly. Might want to shut down.", e);
        }
    }

    @Override
    public void onEnable() {
        final File file = new File(this.getDataFolder(), "config.yml");
        if (!file.exists()) {
            this.saveDefaultConfig();
        }
        this.onlyDangerous = this.getConfig().getBoolean("onlydangerous", true);
        this.thisName = this.getDescription().getName();
        this.logger = Logger.getLogger("PluginCatcher");
        this.getDataFolder().mkdir();
        this.asyncLogFile = new File(this.getDataFolder(), "async.log");
        try {
            final FileHandler handler = new FileHandler(this.asyncLogFile.getAbsolutePath(), true);
            handler.setFormatter(new Frmttr());
            this.logger.addHandler(handler);
            this.logger.setUseParentHandlers(false);
        } catch (final Exception e) {
            e.printStackTrace();
            this.logger = this.getLogger();
        }
        try {
            this.jplLoaders = JavaPluginLoader.class.getDeclaredField("loaders");
            this.jplLoaders.setAccessible(true);
            this.pclClasses = PluginClassLoader.class.getDeclaredField("classes");
            this.pclClasses.setAccessible(true);
            final Field fieldEntities = World.class.getDeclaredField("entityList");
            fieldEntities.setAccessible(true);
            final Field fieldPlayers = World.class.getDeclaredField("players");
            fieldPlayers.setAccessible(true);
            final Field fieldTileEntities = World.class.getDeclaredField("tileEntityList");
            fieldTileEntities.setAccessible(true);
            final Field fieldUnloadQueue = ChunkProviderServer.class.getDeclaredField("unloadQueue");
            fieldUnloadQueue.setAccessible(true);
            final Field fieldEntityTrackerSet = EntityTracker.class.getDeclaredField("b");
            fieldEntityTrackerSet.setAccessible(true);
            for (final org.bukkit.World bworld : this.getServer().getWorlds()) {
                final World world = ((CraftWorld) bworld).getHandle();
                fieldEntities.set(world, new OverlyAttachedArrayList<Entity>(this, (List<Entity>) fieldEntities.get(world)));
                fieldPlayers.set(world, new OverlyAttachedArrayList<EntityHuman>(this, (List<EntityHuman>) fieldPlayers.get(world)));
                fieldTileEntities.set(world, new OverlyAttachedArrayList<TileEntity>(this, (List<TileEntity>) fieldTileEntities.get(world)));
                final EntityTracker tracker = ((WorldServer) world).tracker;
                fieldEntityTrackerSet.set(tracker, new HugSet<EntityTrackerEntry>(this, (Set<EntityTrackerEntry>) fieldEntityTrackerSet.get(tracker)));
            }
        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to start up properly. Might want to shut down.", e);
        }
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Output(), 20, 20);
    }

}