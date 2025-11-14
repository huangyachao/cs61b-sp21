package deque;

import java.util.Iterator;
import java.util.Objects;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {
    private int size;
    private Node<T> head;
    private Node<T> tail;

    public LinkedListDeque() {
        this.size = 0;
        this.head = new Node<T>(null);
        this.tail = new Node<T>(null);
        this.head.prev = this.tail;
        this.head.next = this.tail;
        this.tail.prev = this.head;
        this.tail.next = this.head;
    }

    @Override
    public void addFirst(T item) {
        Node<T> itemNode = new Node<>(item);
        itemNode.prev = this.head;
        itemNode.next = this.head.next;
        this.head.next.prev = itemNode;
        this.head.next = itemNode;
        ++this.size;
    }

    @Override
    public void addLast(T item) {
        Node<T> itemNode = new Node<>(item);
        itemNode.prev = this.tail.prev;
        itemNode.next = this.tail;
        this.tail.prev.next = itemNode;
        this.tail.prev = itemNode;
        ++this.size;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void printDeque() {
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            System.out.print(it.next() + " ");
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        Node<T> node = this.head.next;
        if (node == this.tail) {
            return null;
        }
        T data = node.data;
        node.prev.next = node.next;
        node.next.prev = node.prev;
        --this.size;
        return data;
    }

    @Override
    public T removeLast() {
        Node<T> node = this.tail.prev;
        if (node == this.head) {
            return null;
        }
        T data = node.data;
        node.prev.next = node.next;
        node.next.prev = node.prev;
        --this.size;
        return data;
    }

    @Override
    public T get(int index) {
        Node<T> node = this.head.next;
        for (int i = 0; i < index; i++) {
            node = node.next;
            if (node == this.tail) {
                return null;
            }
        }
        return node.data;
    }

    private T helper(int index, Node<T> node) {
        if (index == 0) {
            return node.data;
        }
        return helper(index - 1, node.next);
    }
    public T getRecursive(int index) {
        if (index < 0 || index >= this.size) {
            return null;
        }
        Node<T> node = this.head.next;
        return helper(index, node);
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

    private static class Node<T> {
        private T data;
        private Node<T> prev;
        private Node<T> next;

        Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }

    private class NodeIterator implements Iterator<T> {
        private Node<T> node = head.next;

        @Override
        public boolean hasNext() {
            return node != tail;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                return null;
            }
            T data = node.data;
            node = node.next;
            return data;
        }
    }
}
