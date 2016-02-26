package com.zhiliao.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by riaqn on 15-8-27.
 */

public abstract class SliceIterator<T> implements Iterator<Slice<T>> {
    private static final Logger logger = LoggerFactory.getLogger(SliceIterator.class);
    private Slice<T> slice = load(new PageRequest(0, 1));

    protected abstract Slice<T> load(Pageable pageable);

    @Override
    public boolean hasNext() {
        return slice != null;
    }

    @Override
    public Slice<T> next() {
        if (slice == null)
            throw new NoSuchElementException();
        Slice<T> slice0 = slice;

        if (slice.hasNext()) {
            Pageable pageable = slice.nextPageable();
            slice = load(pageable);
        } else
            slice = null;

        return slice0;
    }

    public Iterator<T> flatten() {
        Iterator<Slice<T>> sliceIterator = this;
        return new Iterator<T>() {
            private Iterator<T> iterator = new ArrayList<T>().iterator();

            private boolean nextPage() {
                if (!sliceIterator.hasNext())
                    return false;
                iterator = sliceIterator.next().iterator();
                return true;
            }

            @Override
            public boolean hasNext() {
                while (!iterator.hasNext())
                    if (!nextPage())
                        return false;
                return true;
            }

            @Override
            public T next() {
                while (!iterator.hasNext())
                    if (!nextPage())
                        break;
                return iterator.next();
            }
        };
    }
}
