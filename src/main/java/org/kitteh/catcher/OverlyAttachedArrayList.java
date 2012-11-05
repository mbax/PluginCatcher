package org.kitteh.catcher;

import java.util.*;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class OverlyAttachedArrayList extends ArrayList {
    private static final long serialVersionUID = 4671186665144729041L;
    private final Thread thread;

    public OverlyAttachedArrayList(ArrayList list) {
        super(list);
        this.thread = Thread.currentThread();
    }

    @Override
    public void add(int index, Object element) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        super.add(index, element);
    }

    @Override
    public boolean add(Object e) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection c) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection c) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.addAll(index, c);
    }

    @Override
    public void clear() {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        super.clear();
    }

    @Override
    public boolean contains(Object o) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.contains(o);
    }

    @Override
    public boolean containsAll(Collection c) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.containsAll(c);
    }

    @Override
    public Object get(int index) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.get(index);
    }

    @Override
    public int indexOf(Object o) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.isEmpty();
    }

    @Override
    public Iterator<Object> iterator() {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.lastIndexOf(o);
    }

    @Override
    public ListIterator<Object> listIterator() {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.listIterator(index);
    }

    @Override
    public Object remove(int index) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.remove(index);
    }

    @Override
    public boolean remove(Object o) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.remove(o);
    }

    @Override
    public boolean removeAll(Collection c) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection c) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.retainAll(c);
    }

    @Override
    public Object set(int index, Object element) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.set(index, element);
    }

    @Override
    public int size() {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.size();
    }

    @Override
    public List<Object> subList(int fromIndex, int toIndex) {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        if (!Thread.currentThread().equals(this.thread)) {
            new CaughtInTheAct(new Throwable()).printStackTrace();
        }
        return super.toArray();
    }

}
