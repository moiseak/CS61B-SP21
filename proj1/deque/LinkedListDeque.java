package deque;

public class LinkedListDeque <T>{
    public static class ListNode <T>{
        T val;
        ListNode next;
        ListNode prev;
        ListNode(T x){
            val = x;
            next = null;
            prev = null;
        }
    }

    private ListNode sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new ListNode(0);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    public void addFirst(T x) {
        ListNode newNode = new ListNode(x);
        sentinel.next.prev = newNode;
        newNode.next = sentinel.next;
        sentinel.next = newNode;
        newNode.prev = sentinel;
        size += 1;
    }

    public void addLast(T x) {
        ListNode newNode = new ListNode(x);
        sentinel.prev.next = newNode;
        newNode.prev = sentinel.prev;
        sentinel.prev = newNode;
        newNode.next = sentinel;
        size += 1;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        ListNode current = sentinel.next;
        while (current != sentinel) {
            System.out.print(current.val + " ");
            current = current.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        T removeValue = (T) sentinel.next.val;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        size -= 1;
        return removeValue;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        T removeValue = (T) sentinel.prev.val;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        size -= 1;
        return removeValue;
    }

    public T get(int index) {
        ListNode current = sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return (T) current.val;
    }

    public T getRecursive(int index) {
        return getRecursiveHelper(sentinel.next, index);
    }

    private T getRecursiveHelper(ListNode node, int index) {
        if (index == 0) {
            return (T) node.val;
        } else {
            return getRecursiveHelper(node.next, index - 1);
        }
    }

}
