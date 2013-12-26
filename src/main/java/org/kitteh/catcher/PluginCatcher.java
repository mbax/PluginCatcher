/*
 * Copyright 2013 Matt Baxter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http:www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kitteh.catcher;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unchecked")
public class PluginCatcher extends JavaPlugin implements Listener {
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
        @Override
        public void run() {
            boolean found = false;
            while (!PluginCatcher.this.badList.isEmpty()) {
                found = true;
                this.log(PluginCatcher.this.badList.remove(0), "dangerous");
            }
            if (!PluginCatcher.this.onlyDangerous) {
                while (!PluginCatcher.this.riskyList.isEmpty()) {
                    found = true;
                    this.log(PluginCatcher.this.riskyList.remove(0), "risky");
                }
            }
            if (found) {
                PluginCatcher.this.getLogger().severe("Found an async call. Check " + PluginCatcher.this.asyncLogFile);
            }
        }

        private void log(Throwable throwable, String desc) {
            final Set<String> set = new HashSet<String>();
            for (final StackTraceElement element : throwable.getStackTrace()) {
                try {
                    final JavaPlugin plugin = JavaPlugin.getProvidingPlugin(element.getClass());
                    if (!plugin.getName().equals(PluginCatcher.this.getName())) {
                        set.add(plugin.getName());
                    }
                } catch (final Exception e) {
                    continue;
                }
            }
            String message;
            if (set.isEmpty()) {
                message = "Found an async call I can't trace";
            } else {
                final StringBuilder builder = new StringBuilder();
                builder.append("Found " + desc + " async call. Might be from: ");
                for (final String plugin : set) {
                    final Plugin p = PluginCatcher.this.getServer().getPluginManager().getPlugin(plugin);
                    builder.append(plugin).append(" ").append(p.getDescription().getVersion()).append(" ");
                }
                message = builder.toString();
            }
            PluginCatcher.this.logger.log(Level.WARNING, message, throwable);
        }
    }

    private boolean failed = false;
    private Logger logger;
    private File asyncLogFile;
    private final List<Throwable> riskyList = Collections.synchronizedList(new ArrayList<Throwable>());
    private final List<Throwable> badList = Collections.synchronizedList(new ArrayList<Throwable>());
    private boolean onlyDangerous;
    private Class<?> classCraftWorld;
    private Class<?> classWorld;
    private Method methodGetHandle;
    private YamlConfiguration reflectionConfig;
    private CharSequence supportedVersion;
    private Field fieldTracker;

    private Map<Class<?>, Set<Field>> worldFields;

    private Map<Class<?>, Set<Field>> trackerFields;

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
        if (this.failed) {
            return;
        }
        try {
            for (final World bworld : this.getServer().getWorlds()) {
                final Object world = this.methodGetHandle.invoke(bworld);
                this.unHookCollections(world, this.worldFields);
                final Object tracker = this.fieldTracker.get(world);
                this.unHookCollections(tracker, this.trackerFields);
            }
        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to disable properly. Might want to shut down.", e);
        }
    }

    @Override
    public void onEnable() {
        final String packageName = this.getServer().getClass().getPackage().getName();
        final String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        this.reflectionConfig = YamlConfiguration.loadConfiguration(this.getResource("reflection.yml"));
        this.supportedVersion = this.reflectionConfig.getString("version");
        if (!version.equals(this.supportedVersion)) {
            this.getLogger().severe("Incompatible versions. Supports " + this.supportedVersion + ", found " + version);
            this.failed = true;
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        final File file = new File(this.getDataFolder(), "config.yml");
        if (!file.exists()) {
            this.saveDefaultConfig();
        }
        if (this.getConfig().getBoolean("meow", true)) {
            this.getLogger().info("             |\\__/,|   (`\\");
            this.getLogger().info("             |o o  |__ _) )");
            this.getLogger().info("           _.( T   )  `  /");
            this.getLogger().info(" n n._    ((_ `^--' /_<  \\");
            this.getLogger().info(" <\" _ }=- `` `-'(((/  (((/");
            this.getLogger().info("  `\" \"");
        }
        this.getServer().getPluginManager().registerEvents(this, this);
        this.onlyDangerous = this.getConfig().getBoolean("onlydangerous", true);
        this.logger = Logger.getLogger("PluginCatcher");
        this.getDataFolder().mkdir();
        this.asyncLogFile = new File(this.getDataFolder(), "async.log");
        String logLocation;
        try {
            final FileHandler handler = new FileHandler(this.asyncLogFile.getAbsolutePath(), true);
            handler.setFormatter(new Frmttr());
            this.logger.addHandler(handler);
            this.logger.setUseParentHandlers(false);
            final File parentFile = this.getDataFolder().getParentFile();
            logLocation = (parentFile != null ? (parentFile.getName() + "/") : "") + this.getDataFolder().getName() + "/async.log";
        } catch (final Exception e) {
            this.logger = this.getLogger();
            this.logger.severe("Could not load custom log. Reverting to server.log");
            e.printStackTrace();
            logLocation = "your server's log file";
        }
        try {
            this.classCraftWorld = Class.forName("org.bukkit.craftbukkit." + this.supportedVersion + ".CraftWorld");
            this.classWorld = this.getClass("world.nms");
            final Class<?> classWorldServer = this.getClass("world.server");
            this.methodGetHandle = this.classCraftWorld.getDeclaredMethod("getHandle");
            this.worldFields = this.getFieldMap(this.classWorld, this.reflectionConfig.getStringList("world.fields"));
            this.fieldTracker = classWorldServer.getDeclaredField(this.reflectionConfig.getString("world.tracker.field"));

            final Class<?> classTracker = this.getClass("world.tracker.nms");
            this.trackerFields = this.getFieldMap(classTracker, this.reflectionConfig.getStringList("world.tracker.fields"));

            for (final World world : this.getServer().getWorlds()) {
                this.hookWorld(world);
            }
        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to start up properly. Might want to shut down.", e);
        }
        this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Output(), 20, 20);
        this.getLogger().info("If something goes wrong, I'll log it to " + logLocation);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        try {
            this.hookWorld(event.getWorld());
        } catch (final Exception e) {
            this.getLogger().log(Level.SEVERE, "Failed to hook world " + event.getWorld().getName() + ".", e);
        }
    }

    private Class<?> getClass(String path) throws ClassNotFoundException {
        return Class.forName(this.version(this.reflectionConfig.getString(path)));
    }

    private Map<Class<?>, Set<Field>> getFieldMap(Class<?> clazz, List<String> fieldNames) throws NoSuchFieldException, SecurityException {
        final Map<Class<?>, Set<Field>> map = new HashMap<Class<?>, Set<Field>>();
        map.put(List.class, new HashSet<Field>());
        map.put(Set.class, new HashSet<Field>());
        for (final String fieldName : fieldNames) {
            final Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            if (field.getType().isAssignableFrom(List.class)) {
                map.get(List.class).add(field);
            } else if (field.getType().isAssignableFrom(Set.class)) {
                map.get(Set.class).add(field);
            }
        }
        return map;
    }

    private void hookCollections(Object object, Map<Class<?>, Set<Field>> fields) throws IllegalArgumentException, IllegalAccessException {
        for (final Field field : fields.get(List.class)) {
            Object o = field.get(object);
            o = new OverlyAttachedArrayList<Object>(this, (List<Object>) o);
            field.set(object, o);
        }
        for (final Field field : fields.get(Set.class)) {
            Object o = field.get(object);
            o = new HugSet<Object>(this, (Set<Object>) o);
            field.set(object, o);
        }
    }

    private void hookWorld(World bworld) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Object world = this.methodGetHandle.invoke(bworld);
        this.hookCollections(world, this.worldFields);
        final Object tracker = this.fieldTracker.get(world);
        this.hookCollections(tracker, this.trackerFields);
    }

    private void unHookCollections(Object object, Map<Class<?>, Set<Field>> fields) throws IllegalArgumentException, IllegalAccessException {
        for (final Field field : fields.get(List.class)) {
            Object o = field.get(object);
            o = new ArrayList<Object>((List<Object>) o);
            field.set(object, o);
        }
        for (final Field field : fields.get(Set.class)) {
            Object o = field.get(object);
            o = new HashSet<Object>((Set<Object>) o);
            field.set(object, o);
        }
    }

    private String version(String path) {
        return path.replace("@", this.supportedVersion);
    }
}