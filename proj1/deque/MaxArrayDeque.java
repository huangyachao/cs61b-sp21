package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comp;

    public MaxArrayDeque(Comparator<T> c) {
        this.comp = c;
    }

    public T max() {
        return max(comp);
    }

    public T max(Comparator<T> c) {
        T max = null;
        NodeIterator iterator = new NodeIterator();
        while (iterator.hasNext()) {
            T data = iterator.next();
            if (max == null || c.compare(data, max) > 0) {
                max = data;
            }
        }
        return max;
    }
}