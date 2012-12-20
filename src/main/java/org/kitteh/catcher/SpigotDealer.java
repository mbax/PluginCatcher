package org.kitteh.catcher;

import java.lang.reflect.Field;

import net.minecraft.server.v1_4_5.ChunkProviderServer;

import org.bukkit.craftbukkit.v1_4_5.util.FlatSet;

public class SpigotDealer {
    public static void disable(ChunkProviderServer server) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field fe = ChunkProviderServer.class.getDeclaredField("unloadQueue");
        fe.setAccessible(true);
        final Object o = fe.get(server);
        if (o instanceof RotundSet) {
            final FlatSet oldFlatSet = (FlatSet) o;
            final FlatSet newFlatSet = new FlatSet();
            RotundSet.swappy(oldFlatSet, newFlatSet);
            fe.set(server, newFlatSet);
        }
    }

    public static void enable(ChunkProviderServer server, Plugin plugin) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        final Field fe = ChunkProviderServer.class.getDeclaredField("unloadQueue");
        fe.setAccessible(true);
        final Object o = fe.get(server);
        if (o instanceof FlatSet) {
            final FlatSet oldFlatSet = (FlatSet) o;
            final RotundSet newFlatSet = new RotundSet(plugin);
            RotundSet.swappy(oldFlatSet, newFlatSet);
            fe.set(server, newFlatSet);
        }
    }
}
