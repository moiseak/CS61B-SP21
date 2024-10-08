package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        BuggyAList<Integer> blist = new BuggyAList<>();
        AListNoResizing<Integer> alist = new AListNoResizing<>();
        for (int i = 0; i < 10; i++) {
            blist.addLast(i);
            alist.addLast(i);
        }

        assertEquals(alist.removeLast(), blist.removeLast());
        assertEquals(alist.removeLast(), blist.removeLast());
        assertEquals(alist.removeLast(), blist.removeLast());
    }

    @Test
    public void test() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> blist = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                blist.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                if (size > 0) {
                    int remove1 = L.removeLast();
                    int remove2 = blist.removeLast();
                    assertEquals(remove1, remove2);
                }

            } else if (operationNumber == 2) {
                int size = L.size();
                if (size > 0) {
                    int last = L.getLast();
                    int last2 = blist.getLast();
                    assertEquals(last, last2);
                }
            }
        }
    }

}
