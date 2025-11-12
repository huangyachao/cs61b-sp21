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
    public void testBuggyAList() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> L2 = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                L2.addLast(randVal);
            }  else if (operationNumber == 1) {
                // size
                int size = L.size();
            }
            else if (operationNumber == 2) {
                int size = L.size();
                if(size > 0){
                    Integer lastItem1 =L.getLast();
                    Integer lastItem2 =L2.getLast();
                    assertEquals(lastItem1, lastItem2);
                }
            } else if (operationNumber == 3) {
                int size = L.size();
                if(size > 0){
                    Integer lastItem1 =L.removeLast();
                    Integer lastItem2 =L2.removeLast();
                    assertEquals(lastItem1, lastItem2);
                }
            }
        }
    }
}
