package deque;

public class ArrayDeque <T>{
    private T[] array;
    int size;
    int nextlast;
    int nextfirst;
    public ArrayDeque() {
        array = (T[]) new Object[8];
        size = 0;
        nextlast = 0;
        nextfirst = 0;
    }

    public T[] resize(int newSize) {
        T[] newArray = (T[]) new Object[newSize];
        System.arraycopy(array, 0, newArray, 0, size);
        return newArray;
    }

    public void addFirst(T item) {
        if (size == array.length) {
            array = resize(size * 2);
        }
        nextfirst = (nextfirst - 1 + array.length) % array.length;
        array[nextfirst] = item;
        size++;
    }

    public void addLast(T item) {
        if (size == array.length) {
            array = resize(size * 2);
        }
        nextlast = (nextlast + 1) % array.length;
        array[nextlast] = item;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T item = array[0];
        size--;
        nextfirst = (nextfirst + 1) % array.length;
        return array[nextfirst];
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        size--;
        nextlast = (nextlast - 1 + array.length) % array.length;
        return array[nextlast];
    }
}
