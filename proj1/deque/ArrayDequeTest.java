package deque;

import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Moiads
 */
public class ArrayDequeTest {
    @Test
    public void addRemoveTest() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        assertTrue(deque.isEmpty());
        deque.addFirst(1);
        deque.addFirst(2);
        deque.addFirst(3);
        deque.addLast(4);
        deque.addLast(5);
        deque.addLast(6);
        deque.addLast(7);
        deque.addLast(8);
        deque.addLast(9);
        deque.removeFirst();
        deque.removeFirst();
        deque.removeLast();
        deque.removeLast();
        assertEquals(5, deque.size());
        deque.printDeque();
    }

    @Test
    public void bigDequeTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        for (int i = 0; i < 1000000; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 500000; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 999999; i > 500000; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    public void test1() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        deque.addFirst(1);
        deque.addFirst(2);
        deque.addFirst(3);
        deque.removeFirst();
        Iterator<Integer> iter = deque.iterator();

        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

    @Test
    public void test2() {
        ArrayDeque<Integer> deque = new ArrayDeque<>();
        ArrayDeque<Integer> deque2 = new ArrayDeque<>();
        LinkedListDeque<Integer> linkedListDeque = new LinkedListDeque<>();
        deque.addFirst(1);
        deque.addFirst(2);
        deque.addFirst(3);
        deque2.addFirst(1);
        deque2.addFirst(2);
        deque2.addFirst(3);
        linkedListDeque.addFirst(1);
        linkedListDeque.addFirst(2);
        linkedListDeque.addFirst(3);
        assert deque.equals(deque);
        assert deque.equals(linkedListDeque);
        assert deque.equals(deque2);

    }
}
