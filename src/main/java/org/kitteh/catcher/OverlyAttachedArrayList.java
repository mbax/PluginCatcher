package org.kitteh.catcher;

import java.util.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class OverlyAttachedArrayList extends ArrayList {
    private static final long serialVersionUID = 4671186665144729041L;
    private final Thread thread;
    private final Plugin plugin;

    public OverlyAttachedArrayList(Plugin plugin, ArrayList list) {
        super(list);
        this.plugin = plugin;
        this.thread = Thread.currentThread();
    }

    @Override
    public void add(int index, Object element) {
        this.check();
        super.add(index, element);
    }

    @Override
    public boolean add(Object e) {
        this.check();
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection c) {
        this.check();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        this.check();
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        this.check();
        super.clear();
    }

    @Override
    public boolean contains(Object o) {
        this.check();
        return super.contains(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        this.check();
        return super.containsAll(c);
    }

    @Override
    public Object get(int index) {
        this.check();
        return super.get(index);
    }

    @Override
    public int indexOf(Object o) {
        this.check();
        return super.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        this.check();
        return super.isEmpty();
    }

    @Override
    public Iterator<Object> iterator() {
        this.check();
        return super.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        this.check();
        return super.lastIndexOf(o);
    }

    @Override
    public ListIterator<Object> listIterator() {
        this.check();
        return super.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        this.check();
        return super.listIterator(index);
    }

    @Override
    public Object remove(int index) {
        this.check();
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        this.check();
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection c) {
        this.check();
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        this.check();
        return super.retainAll(c);
    }

    @Override
    public Object set(int index, Object element) {
        this.check();
        return super.set(index, element);
    }

    @Override
    public int size() {
        this.check();
        return super.size();
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        this.check();
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        this.check();
        return super.toArray();
    }

    private void check() {
        if (!Thread.currentThread().equals(this.thread)) {
            this.plugin.list.add(new Throwable().fillInStackTrace());
        }
    }

}
