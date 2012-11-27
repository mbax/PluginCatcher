package org.kitteh.catcher;

/*
  Based on CompactHashSet Copyright 2011 Ontopia Project

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

import java.lang.reflect.Field;
import java.util.Iterator;

import org.bukkit.craftbukkit.util.LongHash;
import org.bukkit.craftbukkit.util.LongHashSet;

public class LongHugSet extends LongHashSet {
    //Ah, the...
    public static void olBukkitSwitcharoo(LongHashSet mommy, LongHashSet baby) {
        try {
            Field fe = LongHashSet.class.getDeclaredField("freeEntries");
            fe.setAccessible(true);
            fe.setInt(baby, fe.getInt(mommy));
            fe = LongHashSet.class.getDeclaredField("elements");
            fe.setAccessible(true);
            fe.setInt(baby, fe.getInt(mommy));
            fe = LongHashSet.class.getDeclaredField("values");
            fe.setAccessible(true);
            fe.set(baby, fe.get(mommy));
            fe = LongHashSet.class.getDeclaredField("modCount");
            fe.setAccessible(true);
            fe.setInt(baby, fe.getInt(mommy));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    private final Thread thread;

    private final Plugin plugin;

    public LongHugSet(LongHashSet mommy, Plugin plugin) {
        super(mommy.size());

        this.thread = Thread.currentThread();
        this.plugin = plugin;
    }

    @Override
    public boolean add(int msw, int lsw) {
        this.check();
        return super.add(msw, lsw);
    }

    @Override
    public boolean add(long value) {
        this.check();
        return super.add(value);
    }

    @Override
    public void clear() {
        this.check();
        super.clear();
    }

    @Override
    public boolean contains(int msw, int lsw) {
        this.check();
        return this.contains(LongHash.toLong(msw, lsw));
    }

    @Override
    public boolean contains(long value) {
        this.check();
        return super.contains(value);
    }

    @Override
    public boolean isEmpty() {
        this.check();
        return super.isEmpty();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator iterator() {
        this.check();
        return super.iterator();
    }

    @Override
    public long[] popAll() {
        this.check();
        return super.popAll();
    }

    @Override
    public long popFirst() {
        this.check();
        return super.popFirst();
    }

    @Override
    public void remove(int msw, int lsw) {
        this.check();
        super.remove(msw, lsw);
    }

    @Override
    public boolean remove(long value) {
        this.check();
        return super.remove(value);
    }

    @Override
    public int size() {
        this.check();
        return super.size();
    }

    @Override
    public long[] toArray() {
        this.check();
        return super.toArray();
    }

    private void check() {
        if (!Thread.currentThread().equals(this.thread)) {
            this.plugin.list.add(new Throwable().fillInStackTrace());
        }
    }
}
