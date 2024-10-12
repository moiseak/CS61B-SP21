package deque;

import org.junit.Test;

import java.util.Comparator;

/**
 * @author Moiads
 */
public class MaxArrayDequeTest {
    private static class intComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2;
        }
    }

    public static Comparator<Integer> getIntComparator() {
        return new intComparator();
    }

    @Test
    public void test1() {
        MaxArrayDeque<Integer> m1 = new MaxArrayDeque<>(getIntComparator());
        m1.addLast(1);
        m1.addLast(2);
        m1.addLast(3);
        int m = m1.max();
        System.out.println(m);
    }
}
