package be.twofold.playground.json;

import java.util.*;

final class PeekingImpl<E> implements Iterator<E> {

    private final Iterator<? extends E> iterator;
    private boolean hasPeeked;
    private E peekedElement;

    public PeekingImpl(Iterator<? extends E> iterator) {
        this.iterator = Objects.requireNonNull(iterator);
    }

    @Override
    public boolean hasNext() {
        return hasPeeked || iterator.hasNext();
    }

    @Override
    public E next() {
        if (!hasPeeked) {
            return iterator.next();
        }
        // The cast is safe because of the hasPeeked check.
        E result = peekedElement;
        hasPeeked = false;
        peekedElement = null;
        return result;
    }

    public E peek() {
        if (!hasPeeked) {
            peekedElement = iterator.next();
            hasPeeked = true;
        }
        // The cast is safe because of the hasPeeked check.
        return peekedElement;
    }
}
