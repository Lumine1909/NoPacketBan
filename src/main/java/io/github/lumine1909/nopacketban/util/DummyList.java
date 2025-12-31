package io.github.lumine1909.nopacketban.util;

import java.util.AbstractList;
import java.util.RandomAccess;

public class DummyList extends AbstractList<Object> implements RandomAccess {

    // Immutable so we could use it everywhere
    public static final DummyList INSTANCE = new DummyList();

    @Override
    public Object get(int index) {
        return null;
    }

    @Override
    public int size() {
        return 1;
    }

    // Netty internal won't work correctly if this is empty.
    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean add(Object o) {
        return true;
    }

    @Override
    public void add(int index, Object element) {
    }

    @Override
    public Object set(int index, Object element) {
        return element;
    }
}