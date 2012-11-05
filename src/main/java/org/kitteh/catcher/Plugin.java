package org.kitteh.catcher;

import java.lang.reflect.Field;
import java.util.ArrayList;

import net.minecraft.server.World;

import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Plugin extends JavaPlugin {

    @Override
    public void onDisable() {
        try {
            final Field fieldEntities = World.class.getDeclaredField("entityList");
            fieldEntities.setAccessible(true);
            final Field fieldPlayers = World.class.getDeclaredField("players");
            fieldPlayers.setAccessible(true);
            final Field fieldTileEntities = World.class.getDeclaredField("tileEntityList");
            fieldTileEntities.setAccessible(true);
            for (final org.bukkit.World bworld : this.getServer().getWorlds()) {
                final World world = ((CraftWorld) bworld).getHandle();
                fieldEntities.set(world, new ArrayList((ArrayList) fieldEntities.get(world)));
                fieldPlayers.set(world, new ArrayList((ArrayList) fieldPlayers.get(world)));
                fieldTileEntities.set(world, new ArrayList((ArrayList) fieldTileEntities.get(world)));
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
        try {
            final Field fieldEntities = World.class.getDeclaredField("entityList");
            fieldEntities.setAccessible(true);
            final Field fieldPlayers = World.class.getDeclaredField("players");
            fieldPlayers.setAccessible(true);
            final Field fieldTileEntities = World.class.getDeclaredField("tileEntityList");
            fieldTileEntities.setAccessible(true);
            for (final org.bukkit.World bworld : this.getServer().getWorlds()) {
                final World world = ((CraftWorld) bworld).getHandle();
                fieldEntities.set(world, new OverlyAttachedArrayList((ArrayList) fieldEntities.get(world)));
                fieldPlayers.set(world, new OverlyAttachedArrayList((ArrayList) fieldPlayers.get(world)));
                fieldTileEntities.set(world, new OverlyAttachedArrayList((ArrayList) fieldTileEntities.get(world)));
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

}
