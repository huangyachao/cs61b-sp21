package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int bucketsLenght;
    private int size;
    private double maxLoadFactor;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        bucketsLenght = 16;
        size = 0;
        maxLoadFactor = 0.75;
        buckets = createTable(bucketsLenght);

    }

    public MyHashMap(int initialSize) {
        bucketsLenght = initialSize;
        size = 0;
        maxLoadFactor = 0.75;
        buckets = createTable(bucketsLenght);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        bucketsLenght = initialSize;
        size = 0;
        maxLoadFactor = maxLoad;
        buckets = createTable(bucketsLenght);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] buckets = (Collection<Node>[]) new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            buckets[i] = createBucket();
        }
        return buckets;
    }


    @Override
    public void clear() {
        bucketsLenght = 16;
        size = 0;
        buckets = createTable(bucketsLenght);
    }

    private int hashFuction(int key) {
        return Math.floorMod(key, bucketsLenght);
    }

    @Override
    public boolean containsKey(K key) {
        int bucketIndex = hashFuction(key.hashCode());
        for (Node node : buckets[bucketIndex]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int bucketIndex = hashFuction(key.hashCode());
        for (Node node : buckets[bucketIndex]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private Collection<Node>[] repaceBuckets() {
        bucketsLenght = bucketsLenght * 2;
        Collection<Node>[] newBuckets = createTable(bucketsLenght);
        for (int i = 0; i < buckets.length; i++) {
            for (Node node : buckets[i]) {
                newBuckets[hashFuction(node.key.hashCode())].add(node);
            }
        }
        return newBuckets;
    }

    @Override
    public void put(K key, V value) {
        if ((double) size / (double) bucketsLenght >= maxLoadFactor) {
            buckets = repaceBuckets();
        }
        int bucketIndex = hashFuction(key.hashCode());
        for (Node node : buckets[bucketIndex]) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        buckets[bucketIndex].add(new Node(key, value));
        size++;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (K key : this) {
            set.add(key);
        }
        return set;
    }

    @Override
    public V remove(K key) {
        int bucketIndex = hashFuction(key.hashCode());
        Node removeNode = null;
        for (Node node : buckets[bucketIndex]) {
            if (node.key.equals(key)) {
                removeNode = node;
            }
        }
        if (removeNode != null) {
            boolean result = buckets[bucketIndex].remove(removeNode);
            assert (result);
            size--;
            return removeNode.value;
        }
        return null;
    }

    @Override
    public V remove(K key, V value) {
        int bucketIndex = hashFuction(key.hashCode());
        Node removeNode = new Node(key, value);
        boolean result = buckets[bucketIndex].remove(removeNode);
        if (result) {
            size--;
            return removeNode.value;
        }
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIter();
    }

    /**
     * An iterator that iterates over the keys of the dictionary.
     */
    private class MyHashMapIter implements Iterator<K> {

        /**
         * Stores the current key-value pair.
         */
        private int index;
        private Iterator<Node> collectionIterator;

        /**
         * Create a new ULLMapIter by setting cur to the first node in the
         * linked list that stores the key-value pairs.
         */
        MyHashMapIter() {
            index = 0;
            while (index < bucketsLenght && buckets[index].isEmpty()) {
                index++;
            }

            collectionIterator = null;
            if (index < bucketsLenght) {
                collectionIterator = buckets[index].iterator();
            }
        }

        @Override
        public boolean hasNext() {
            return index < bucketsLenght;
        }

        @Override
        public K next() {
            Node node = collectionIterator.next();

            while (index < bucketsLenght && !collectionIterator.hasNext()) {
                index++;
                if (index < bucketsLenght) {
                    collectionIterator = buckets[index].iterator();
                }
            }
            return node.key;
        }

    }

}
