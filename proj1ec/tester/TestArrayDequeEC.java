package tester;
import static org.junit.Assert.*;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;
public class TestArrayDequeEC {
    @Test
    public void testRandom() {
        StudentArrayDeque<Integer> L = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> L2 = new ArrayDequeSolution<>();
        StringBuilder message= new StringBuilder();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 5);
            if (operationNumber == 0) {
                // addFirst
                int randVal = StdRandom.uniform(0, 100);
                L.addFirst(randVal);
                L2.addFirst(randVal);
                message.append(String.format("addFirst(%d)\n", randVal));
            } else if (operationNumber == 1) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                L2.addLast(randVal);
                message.append(String.format("addLast(%d)\n", randVal));
            } else if (operationNumber == 2) {
                // removeFirst
                int size = L.size();
                if (size > 0) {
                    Integer lastItem1 = L.removeFirst();
                    Integer lastItem2 = L2.removeFirst();
                    message.append(String.format("removeFirst()\n"));
                    assertEquals(message.toString(),lastItem1, lastItem2);
                }
            } else if (operationNumber == 3) {
                //removeFirst
                int size = L.size();
                if (size > 0) {
                    Integer lastItem1 = L.removeLast();
                    Integer lastItem2 = L2.removeLast();
                    message.append(String.format("removeLast()\n"));
                    assertEquals(message.toString(), lastItem1, lastItem2);
                }
            } else if (operationNumber == 4) {
                int size1 = L.size();
                int size2 = L2.size();
                assertEquals(message.toString()+"size()\n", size1, size2);
            }
        }
    }
}
