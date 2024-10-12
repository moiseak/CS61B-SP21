package deque;

import java.util.Comparator;

/**
 * @author Moiads
 */

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comparator = c;
    }

    public T max() {
        if (isEmpty()) {
            return null;
        }
        T max = get(0);
        for (int i = 1; i < size() - 1; i++) {
            if (comparator.compare(max, get(i + 1)) < 0) {
                max = get(i + 1);
            }
        }
        return max;
    }

    public T max(Comparator<T> c) {
        comparator = c;
        if (isEmpty()) {
            return null;
        }
        T max = get(0);
        for (int i = 1; i < size() - 1; i++) {
            if (comparator.compare(max, get(i + 1)) < 0) {
                max = get(i + 1);
            }
        }
        return max;
    }
}
