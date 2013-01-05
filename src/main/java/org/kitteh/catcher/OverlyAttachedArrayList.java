package org.kitteh.catcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.kitteh.catcher.Plugin.Badness;

public class OverlyAttachedArrayList<E> extends ArrayList<E> {
    private static final long serialVersionUID = 4671186665144729042L;
    private final Thread thread;
    private final Plugin plugin;

    public OverlyAttachedArrayList(Plugin plugin, List<E> list) {
        super(list);
        this.plugin = plugin;
        this.thread = Thread.currentThread();
    }

    @Override
    public void add(int index, E element) {
        this.check(Badness.VERY_BAD);
        super.add(index, element);
    }

    @Override
    public boolean add(E e) {
        this.check(Badness.VERY_BAD);
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        this.check(Badness.VERY_BAD);
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        this.check(Badness.VERY_BAD);
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        this.check(Badness.VERY_BAD);
        super.clear();
    }

    @Override
    public boolean contains(Object o) {
        this.check(Badness.RISKY);
        return super.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        this.check(Badness.RISKY);
        return super.containsAll(c);
    }

    @Override
    public E get(int index) {
        this.check(Badness.RISKY);
        return super.get(index);
    }

    @Override
    public int indexOf(Object o) {
        this.check(Badness.RISKY);
        return super.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        this.check(Badness.RISKY);
        return super.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        this.check(Badness.RISKY);
        return new OverlyAttachedIterator(super.iterator());
    }

    @Override
    public int lastIndexOf(Object o) {
        this.check(Badness.RISKY);
        return super.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        this.check(Badness.RISKY);
        return new OverlyAttachedListIterator(super.listIterator());
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        this.check(Badness.RISKY);
        return new OverlyAttachedListIterator(super.listIterator(index));
    }

    @Override
    public E remove(int index) {
        this.check(Badness.VERY_BAD);
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        this.check(Badness.VERY_BAD);
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.check(Badness.VERY_BAD);
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        this.check(Badness.RISKY);
        return super.retainAll(c);
    }

    @Override
    public E set(int index, E element) {
        this.check(Badness.RISKY);
        return super.set(index, element);
    }

    @Override
    public int size() {
        this.check(Badness.RISKY);
        return super.size();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        this.check(Badness.RISKY);
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        this.check(Badness.RISKY);
        return super.toArray();
    }

    private void check(Badness badness) {
        if (!Thread.currentThread().equals(this.thread)) {
            this.plugin.add(new Throwable().fillInStackTrace(), badness);
        }
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

    private class OverlyAttachedListIterator implements ListIterator<E> {

        private ListIterator<E> iterator;

        public OverlyAttachedListIterator(ListIterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public void add(E e) {
            check(Badness.VERY_BAD);
            iterator.add(e);
        }

        @Override
        public boolean hasNext() {
            check(Badness.RISKY);
            return iterator.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            check(Badness.RISKY);
            return iterator.hasPrevious();
        }

        @Override
        public E next() {
            check(Badness.RISKY);
            return iterator.next();
        }

        @Override
        public int nextIndex() {
            check(Badness.RISKY);
            return iterator.nextIndex();
        }

        @Override
        public E previous() {
            check(Badness.RISKY);
            return iterator.previous();
        }

        @Override
        public int previousIndex() {
            check(Badness.RISKY);
            return iterator.previousIndex();
        }

        @Override
        public void remove() {
            check(Badness.VERY_BAD);
            iterator.remove();
        }

        @Override
        public void set(E e) {
            check(Badness.VERY_BAD);
            iterator.set(e);
        }

    }

}