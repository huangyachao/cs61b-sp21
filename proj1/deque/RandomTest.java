package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RandomTest {
    @Test
    public void testBuggyAList() {
        LinkedListDeque<Integer> L = new LinkedListDeque<>();
        ArrayDeque<Integer> L2 = new ArrayDeque<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addFirst(randVal);
                L2.addFirst(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
            } else if (operationNumber == 2) {
                int size = L.size();
                if (size > 0) {
                    Integer lastItem1 = L.get(0);
                    Integer lastItem2 = L2.get(0);
                    assertEquals(lastItem1, lastItem2);
                }
            } else if (operationNumber == 3) {
                int size = L.size();
                if (size > 0) {
                    Integer lastItem1 = L.removeLast();
                    Integer lastItem2 = L2.removeLast();
                    assertEquals(lastItem1, lastItem2);
                }
            }
        }
    }
}
