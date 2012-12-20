package org.kitteh.catcher;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.bukkit.craftbukkit.v1_4_5.util.FlatLookup;
import org.bukkit.craftbukkit.v1_4_5.util.FlatSet;
import org.bukkit.craftbukkit.v1_4_5.util.LongObjectHashMap;

public class RotundSet extends FlatSet {

    @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
    public static void swappy(FlatSet oldFlatSet, FlatSet newFlatSet) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field field = FlatLookup.class.getDeclaredField("mapLookup");
        field.setAccessible(true);
        final LongObjectHashMap newmap = (LongObjectHashMap) field.get(newFlatSet);
        final Set<Map.Entry<Long, Object>> lohmes = ((LongObjectHashMap) field.get(oldFlatSet)).entrySet();
        for (final Map.Entry<Long, Object> e : lohmes) {
            newmap.put(e.getKey(), e.getValue());
        }
        field = FlatLookup.class.getDeclaredField("flatLookup");
        field.setAccessible(true);
        final Object[][] oldArr = (Object[][]) field.get(oldFlatSet);
        final Object[][] newArr = (Object[][]) field.get(newFlatSet);
        field = FlatLookup.class.getDeclaredField("FLAT_LOOKUP_SIZE");
        field.setAccessible(true);
        final int max = field.getInt(oldFlatSet);
        for (int x = 0; x < max; x++) {
            for (int y = 0; y < max; y++) {
                newArr[x][y] = oldArr[x][y];
            }
        }
    }

    private final Thread thread;
    private final Plugin plugin;

    public RotundSet(Plugin plugin) {
        this.thread = Thread.currentThread();
        this.plugin = plugin;
    }

    @Override
    public void add(long msw, long lsw) {
        super.add(msw, lsw);
    }

    @Override
    public boolean contains(long msw, long lsw) {
        return super.contains(msw, lsw);
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public long popFirst() {
        return super.popFirst();
    }

    @Override
    public int size() {
        this.check();
        return super.size();
    }

    private void check() {
        if (!Thread.currentThread().equals(this.thread)) {
            this.plugin.list.add(new Throwable().fillInStackTrace());
        }
    }
}