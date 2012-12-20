package org.kitteh.catcher;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.minecraft.server.v1_4_5.ChunkProviderServer;
import net.minecraft.server.v1_4_5.World;
import net.minecraft.server.v1_4_5.WorldServer;

import org.bukkit.craftbukkit.v1_4_5.CraftWorld;
import org.bukkit.craftbukkit.v1_4_5.util.LongHashSet;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Plugin extends JavaPlugin {

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

        @Override
        public void run() {
            Map<String, PluginClassLoader> loaders;
            try {
                loaders = (Map<String, PluginClassLoader>) Plugin.this.jplLoaders.get(Plugin.this.getPluginLoader());
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
                return;
            } catch (final IllegalAccessException e) {
                e.printStackTrace();
                return;
            }
            boolean found = false;
            while (!Plugin.this.list.isEmpty()) {
                found = true;
                final Throwable t = Plugin.this.list.remove(0);
                final String thisName = Plugin.this.getDescription().getName();
                final Set<String> set = new HashSet<String>();
                for (final String plugin : loaders.keySet()) {
                    if (plugin.equals(thisName)) {
                        continue;
                    }
                    final PluginClassLoader loader = loaders.get(plugin);
                    Map<String, Class<?>> classes;
                    try {
                        classes = (Map<String, Class<?>>) Plugin.this.pclClasses.get(loader);
                    } catch (final IllegalArgumentException e2) {
                        e2.printStackTrace();
                        return;
                    } catch (final IllegalAccessException e2) {
                        e2.printStackTrace();
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
                    builder.append("Found async call. Might be from: ");
                    for (final String plugin : set) {
                        final org.bukkit.plugin.Plugin p = Plugin.this.getServer().getPluginManager().getPlugin(plugin);
                        builder.append(plugin);
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
            if (found) {
                Plugin.this.getLogger().severe("Found an async call. Check " + Plugin.this.asyncLogFile);
            }
        }

    }

    private Field jplLoaders;
    private Field pclClasses;
    private Logger logger;
    private File asyncLogFile;
    public List<Throwable> list = Collections.synchronizedList(new ArrayList<Throwable>());

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
            for (final org.bukkit.World bworld : this.getServer().getWorlds()) {
                final World world = ((CraftWorld) bworld).getHandle();
                fieldEntities.set(world, new ArrayList((ArrayList) fieldEntities.get(world)));
                fieldPlayers.set(world, new ArrayList((ArrayList) fieldPlayers.get(world)));
                fieldTileEntities.set(world, new ArrayList((ArrayList) fieldTileEntities.get(world)));
                final ChunkProviderServer server = ((WorldServer) world).chunkProviderServer;
                final Object object = fieldUnloadQueue.get(server);
                if (object instanceof LongHashSet) {
                    final LongHashSet oldbie = (LongHashSet) object;
                    final LongHashSet newbie = new LongHashSet(oldbie.size());
                    LongHugSet.olBukkitSwitcharoo(oldbie, newbie);
                    fieldUnloadQueue.set(server, newbie);
                } else {
                    SpigotDealer.disable(server);
                }
            }
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
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
            for (final org.bukkit.World bworld : this.getServer().getWorlds()) {
                final World world = ((CraftWorld) bworld).getHandle();
                fieldEntities.set(world, new OverlyAttachedArrayList(this, (ArrayList) fieldEntities.get(world)));
                fieldPlayers.set(world, new OverlyAttachedArrayList(this, (ArrayList) fieldPlayers.get(world)));
                fieldTileEntities.set(world, new OverlyAttachedArrayList(this, (ArrayList) fieldTileEntities.get(world)));
                final ChunkProviderServer server = ((WorldServer) world).chunkProviderServer;
                final Object object = fieldUnloadQueue.get(server);
                if (object instanceof LongHashSet) {
                    fieldUnloadQueue.set(server, new LongHugSet((LongHashSet) object, this));
                } else {
                    SpigotDealer.enable(server, this);
                }
            }
        } catch (final SecurityException e) {
            e.printStackTrace();
        } catch (final NoSuchFieldException e) {
            e.printStackTrace();
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        }
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Output(), 20, 20);
    }

}
