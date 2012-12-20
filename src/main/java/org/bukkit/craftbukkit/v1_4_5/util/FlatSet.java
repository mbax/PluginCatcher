package org.bukkit.craftbukkit.v1_4_5.util;

import java.util.Iterator;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FlatSet extends FlatLookup {

    private static final Object PRESENT = new Object();

    public void add(long msw, long lsw) {
        this.put(msw, lsw, FlatSet.PRESENT);
    }

    public boolean contains(long msw, long lsw) {
        return this.containsKey(msw, lsw);
    }

    public boolean isEmpty() {
        return this.mapLookup.isEmpty();
    }

    public long popFirst() {
        final Iterator iter = this.mapLookup.keySet().iterator();
        final long ret = (Long) iter.next();
        iter.remove();
        return ret;
    }

    public int size() {
        return this.mapLookup.size();
    }
}