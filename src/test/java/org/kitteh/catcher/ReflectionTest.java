package org.kitteh.catcher;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

public final class ReflectionTest {
    private final YamlConfiguration reflectionConfig = YamlConfiguration.loadConfiguration(this.getClass().getClassLoader().getResourceAsStream("reflection.yml"));;
    private final String supportedVersion = this.reflectionConfig.getString("version");

    @Test
    public void craftWorldClass() {
        try {
            final Class<?> classCraftWorld = Class.forName("org.bukkit.craftbukkit." + this.supportedVersion + ".CraftWorld");
            classCraftWorld.getDeclaredMethod("getHandle");
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    @Test
    public void nmsWorld() {
        try {
            final Class<?> classWorld = this.getClass("world.nms");
            this.getFieldMap(classWorld, this.reflectionConfig.getStringList("world.fields"));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    @Test
    public void tracker() {
        Class<?> classTracker;
        try {
            classTracker = this.getClass("world.tracker.nms");
            this.getFieldMap(classTracker, this.reflectionConfig.getStringList("world.tracker.fields"));
        } catch (final Exception e) {
            e.printStackTrace();
            throw new AssertionError(e.getMessage());
        }
    }

    @Test
    public void worldServer() {
        Class<?> classWorldServer;
        try {
            classWorldServer = this.getClass("world.server");
            classWorldServer.getDeclaredField(this.reflectionConfig.getString("world.tracker.field"));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage());
        }
    }

    private Class<?> getClass(String path) throws ClassNotFoundException {
        final String string = this.reflectionConfig.getString(path);
        if (string == null) {
            throw new AssertionError("No reflection.yml path " + path);
        }
        return Class.forName(string.replace("@", this.supportedVersion));
    }

    private void getFieldMap(Class<?> clazz, List<String> fieldNames) throws NoSuchFieldException, SecurityException {
        for (final String fieldName : fieldNames) {
            final Field field = clazz.getDeclaredField(fieldName);
            if (!field.getType().isAssignableFrom(List.class) && !field.getType().isAssignableFrom(Set.class)) {
                throw new AssertionError("Field " + field.getName() + " is not a List or Set");
            }
        }
    }
}