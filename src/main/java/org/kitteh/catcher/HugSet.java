package org.kitteh.catcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.kitteh.catcher.Plugin.Badness;

public class HugSet<E> extends HashSet<E> {
    private static final long serialVersionUID = 2781726251854737364L;

    private final Thread thread;
    private final Plugin plugin;

    public HugSet(Plugin plugin, Collection<E> list) {
        super(list);
        this.plugin = plugin;
        this.thread = Thread.currentThread();
    }

    private void check(Badness badness) {
        if (plugin == null) {
            return;
        }
        if (!Thread.currentThread().equals(this.thread)) {
            this.plugin.add(new Throwable().fillInStackTrace(), badness);
        }
    }

    @Override
    public boolean add(E e) {
        check(Badness.VERY_BAD);
        return super.add(e);
    }

    @Override
    public boolean remove(Object o) {
        check(Badness.VERY_BAD);
        return super.remove(o);
    }

    @Override
    public Iterator<E> iterator() {
        check(Badness.RISKY);
        return new OverlyAttachedIterator(super.iterator());
    }
    
    private class OverlyAttachedIterator implements Iterator<E> {

        private Iterator<E> iterator;

        public OverlyAttachedIterator(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            check(Badness.RISKY);
            return iterator.hasNext();
        }

        @Override
        public E next() {
            check(Badness.RISKY);
            return iterator.next();
        }

        @Override
        public void remove() {
            check(Badness.VERY_BAD);
            iterator.remove();
        }

    }
}