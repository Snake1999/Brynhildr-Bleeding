package io.nukkit.util;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NonNullList<E> extends AbstractList<E> {
    private final List<E> delegateIn;
    private final E listType;

    public static <E> NonNullList<E> create() {
        return new NonNullList<>();
    }

    public static <E> NonNullList<E> withSize(int size, E fill) {
        Preconditions.checkNotNull(fill);
        Object[] aobject = new Object[size];
        Arrays.fill(aobject, fill);
        //noinspection unchecked
        return new NonNullList<>(Arrays.asList((E[]) aobject), fill);
    }

    public static <E> NonNullList<E> withElements(E listType, E... elements) {
        return new NonNullList<E>(Arrays.asList(elements), listType);
    }

    protected NonNullList() {
        this(new ArrayList<>(), null);
    }

    protected NonNullList(List<E> delegateIn, @Nullable E listType) {
        this.delegateIn = delegateIn;
        this.listType = listType;
    }

    @Nonnull
    @Override
    public E get(int index) {
        return this.delegateIn.get(index);
    }

    @Override
    public E set(int index, E element) {
        Preconditions.checkNotNull(element);
        return this.delegateIn.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        Preconditions.checkNotNull(element);
        this.delegateIn.add(index, element);
    }

    @Override
    public E remove(int index) {
        return this.delegateIn.remove(index);
    }

    @Override
    public int size() {
        return this.delegateIn.size();
    }

    @Override
    public void clear() {
        if (this.listType == null) {
            super.clear();
        } else {
            for (int i = 0; i < this.size(); ++i) {
                this.set(i, this.listType);
            }
        }
    }
}
