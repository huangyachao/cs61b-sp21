package deque;

import java.util.Iterator;
import java.util.Objects;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {
    private int front;
    private int size;
    private int capacity;
    private T[] data;

    public ArrayDeque() {
        this.front = 0;
        this.capacity = 8;
        this.size = 0;
        this.data = (T[]) new Object[this.capacity];
    }

    private void resizeNewArray(int newCapacity) {

        T[] temp = (T[]) new Object[newCapacity];
        for (int i = 0; i < size(); i++) {
            temp[i] = data[(front + i) % capacity];
        }
        capacity = newCapacity;
        data = temp;
        front = 0;
    }

    @Override
    public void addFirst(T item) {
        if (size() == capacity) {
            resizeNewArray(capacity * 2);
        }
        front = (front - 1 + capacity) % capacity;
        data[front] = item;
        size++;
    }

    @Override
    public void addLast(T item) {
        if (size() == capacity) {
            resizeNewArray(capacity * 2);
        }
        data[(front + size()) % capacity] = item;
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        ArrayDeque<T>.NodeIterator it = new ArrayDeque<T>.NodeIterator();
        while (it.hasNext()) {
            System.out.print(it.next() + " ");
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (size() == 0) {
            return null;
        }
        if (size() >= 16 && size() <= capacity / 4) {
            resizeNewArray(capacity / 4);
        }
        T temp = data[front];
        data[front] = null;
        front = (front + 1) % capacity;
        --size;
        return temp;
    }

    @Override
    public T removeLast() {
        if (size() == 0) {
            return null;
        }
        if (size() >= 16 && size() <= capacity / 4) {
            resizeNewArray(capacity / 4);
        }
        int back = (front + size - 1) % capacity;
        T temp = data[back];
        data[(back) % capacity] = null;
        --size;
        return temp;
    }

    @Override
    public T get(int index) {
        if (index >= size()) {
            return null;
        }
        return data[(front + index) % capacity];
    }

    @Override
    public Iterator<T> iterator() {
        return new NodeIterator();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Deque<?>)) {
            return false;
        }

        Deque<?> other = (Deque<?>) o;

        if (this.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < size(); i++) {
            T a = this.get(i);
            Object b = other.get(i);
            if (!Objects.equals(a, b)) {
                return false;
            }
        }

        return true;
    }

    private class NodeIterator implements Iterator<T> {
        private int index = front;

        @Override
        public boolean hasNext() {
            return index != (front + size()) % capacity;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                return null;
            }
            T temp = data[index];
            index = (index + 1) % capacity;
            return temp;
        }
    }

}
