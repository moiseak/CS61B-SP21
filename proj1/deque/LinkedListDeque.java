package deque;


import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Iterator;

/**
 * @author Moiads
 */
public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private final ListNode<T> sentinel;
    private int size;

    private static class ListNode<T> {
        T val;
        ListNode<T> next;
        ListNode<T> prev;

        ListNode(T x) {
            val = x;
            next = null;
            prev = null;
        }
    }

    public LinkedListDeque() {
        sentinel = new ListNode<>(null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    private class LinkedListIterator implements Iterator<T> {
        private ListNode<T> current;

        public LinkedListIterator() {
            current = sentinel;
        }

        @Override
        public boolean hasNext() {
            return current.next.val != null;
        }

        @Override
        public T next() {
            T ret = current.next.val;
            current = current.next;
            return ret;
        }
    }

    @Override
    @NonNull
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Deque) {
            LinkedListDeque<T> other = (LinkedListDeque<T>) o;
            if (size() != other.size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                if (this.get(i) != other.get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void addFirst(T x) {
        ListNode<T> newNode = new ListNode<>(x);
        sentinel.next.prev = newNode;
        newNode.next = sentinel.next;
        sentinel.next = newNode;
        newNode.prev = sentinel;
        size += 1;
    }

    @Override
    public void addLast(T x) {
        ListNode<T> newNode = new ListNode<>(x);
        sentinel.prev.next = newNode;
        newNode.prev = sentinel.prev;
        sentinel.prev = newNode;
        newNode.next = sentinel;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        ListNode<T> current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.val + " ");
            current = current.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T removeValue = sentinel.next.val;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        size -= 1;
        return removeValue;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T removeValue = sentinel.prev.val;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        size -= 1;
        return removeValue;
    }

    @Override
    public T get(int index) {
        ListNode<T> current = sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.val;
    }

    public T getRecursive(int index) {
        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(ListNode<T> node, int index) {
        if (index == 0) {
            return node.val;
        } else {
            return getRecursiveHelper(node.next, index - 1);
        }
    }

}
