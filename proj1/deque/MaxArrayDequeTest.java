package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MaxArrayDequeTest {
    @Test
    public void testMaxInteger() {

        class GreaterComparator implements Comparator<Integer> {
            @Override
            public int compare(Integer s1, Integer s2) {
                if (s1 > s2) return 1;
                else if (s1 < s2) return -1;
                else return 0;
            }
        }

        class LessComparator implements Comparator<Integer> {
            @Override
            public int compare(Integer s1, Integer s2) {
                if (s1 < s2) return 1;
                else if (s1 > s2) return -1;
                else return 0;
            }
        }


        GreaterComparator gc = new GreaterComparator();
        LessComparator lc = new LessComparator();
        MaxArrayDeque<Integer> deque1 = new MaxArrayDeque<>(gc);

        int max = 1000;
        for (int i = 0; i <= max; ++i) {
            deque1.addLast(i);
        }
        assertEquals((long) max, (long) deque1.max());
        assertEquals((long) 0, (long) deque1.max(lc));
        MaxArrayDeque<Integer> deque2 = new MaxArrayDeque<>(lc);

        for (int i = 0; i <= max; ++i) {
            deque2.addLast(-i);
        }
        assertEquals((long) -max, (long) deque2.max());

        MaxArrayDeque<Integer> deque3 = new MaxArrayDeque<>(gc);
        assertNull(deque3.max());
    }
}
