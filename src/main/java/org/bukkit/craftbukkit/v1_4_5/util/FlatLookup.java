package org.bukkit.craftbukkit.v1_4_5.util;

import java.util.Collection;

import org.bukkit.craftbukkit.v1_4_5.util.LongHash;
import org.bukkit.craftbukkit.v1_4_5.util.LongObjectHashMap;

@SuppressWarnings("unchecked")
public class FlatLookup<E> {

    private static final int FLAT_LOOKUP_SIZE = 512;
    private final Object[][] flatLookup = new Object[FlatLookup.FLAT_LOOKUP_SIZE * 2][FlatLookup.FLAT_LOOKUP_SIZE * 2];
    protected final LongObjectHashMap<E> mapLookup = new LongObjectHashMap<E>();

    public boolean containsKey(long key) {
        return this.containsKey(LongHash.msw(key), LongHash.lsw(key));
    }

    public boolean containsKey(long msw, long lsw) {
        return this.get(msw, lsw) != null;
    }

    public E get(long hash) {
        return this.get(LongHash.msw(hash), LongHash.lsw(hash));
    }

    public E get(long msw, long lsw) {
        // Long is used to avoid integer overflow
        final long acx = Math.abs(msw);
        final long acz = Math.abs(lsw);
        E value;
        if ((acx < FlatLookup.FLAT_LOOKUP_SIZE) && (acz < FlatLookup.FLAT_LOOKUP_SIZE)) {
            value = (E) this.flatLookup[(int) (msw + FlatLookup.FLAT_LOOKUP_SIZE)][(int) (lsw + FlatLookup.FLAT_LOOKUP_SIZE)];
        } else {
            value = this.mapLookup.get(LongHash.toLong((int) msw, (int) lsw));
        }
        return value;
    }

    public void put(long key, E value) {
        this.put(LongHash.msw(key), LongHash.lsw(key), value);
    }

    public void put(long msw, long lsw, E value) {
        final long acx = Math.abs(msw);
        final long acz = Math.abs(lsw);
        if ((acx < FlatLookup.FLAT_LOOKUP_SIZE) && (acz < FlatLookup.FLAT_LOOKUP_SIZE)) {
            this.flatLookup[(int) (msw + FlatLookup.FLAT_LOOKUP_SIZE)][(int) (lsw + FlatLookup.FLAT_LOOKUP_SIZE)] = value;
        }
        if (value == null) {
            this.mapLookup.remove(LongHash.toLong((int) msw, (int) lsw));
        } else {
            this.mapLookup.put(LongHash.toLong((int) msw, (int) lsw), value);
        }
    }

    public void remove(long key) {
        this.remove(LongHash.msw(key), LongHash.lsw(key));
    }

    public void remove(long msw, long lsw) {
        this.put(msw, lsw, null);
    }

    public Collection<E> values() {
        return this.mapLookup.values();
    }
}