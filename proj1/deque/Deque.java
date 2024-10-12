package deque;

/**
 * @author Moiads
 */
public interface Deque<T> {
    default boolean isEmpty() {
        return size() == 0;
    }

    int size();

    void addFirst(T element);

    void addLast(T element);

    T removeFirst();

    T removeLast();

    T get(int index);

    void printDeque();
}
