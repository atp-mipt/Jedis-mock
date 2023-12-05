package com.github.fppt.jedismock.datastructures.streams;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Reverse iterator for {@link LinkedMap LinkedMap}
 *
 * @param <K> keys type, must implement {@link java.lang.Comparable Comparable}
 * @param <V> values type
 */
public class LinkedMapReverseIterator<K extends Comparable<K>, V> implements LinkedMapIterator<K, V> {
    /**
     * Iterator takes place after this key. If is {@code null} then iterator takes place after the tail of the map.
     */
    private K curr;

    /**
     * Map that iterator refers to
     */
    private final LinkedMap<K, V> map;

    public LinkedMapReverseIterator(K curr, LinkedMap<K, V> map) {
        this.map = map;
        this.curr = curr == null ? null : map.getNextKey(curr); // null is possible when map.size == 0
    }

    @Override
    public boolean hasNext() {
        if (curr == null) {
            return map.getTail() != null;
        }

        return map.getPreviousKey(curr) != null;
    }

    @Override
    public Map.Entry<K, V> next() {
        if (!hasNext()) {
            throw new NoSuchElementException("There is no elements left");
        }

        if (curr == null) {
            curr = map.getTail();
        } else {
            curr = map.getPreviousKey(curr);
        }

        return Map.entry(curr, map.get(curr));
    }

    /**
     * Sets {@code curr} equal to the first element not less than {@code border}.
     * If {@code border} is {@code null} than {@code NullPointerException} is thrown.
     * @param border must be not null
     */
    @Override
    public void findFirstSuitable(K border) {
        if (border == null) {
            throw new NullPointerException("Border is null");
        }

        if (map.contains(border)) {
            curr = border;
        }

        if (curr != null && border.compareTo(curr) > 0) { // reset curr
            curr = null;
        }

        if (border != (curr == null ? map.getTail() : curr)) { // searching the first node
            while (hasNext()) {
                next();

                if (curr.compareTo(border) <= 0) {
                    break;
                }
            }
        }

        curr = curr == null ? null : map.getNextKey(curr); // map might be empty
    }
}
