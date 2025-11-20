package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private TreeNode root = null;
    private V removeValue = null;

    private int size(TreeNode node) {
        if (node == null) {
            return 0;
        }
        return node.size;
    }

    @Override
    public void clear() {
        root = null;
    }

    private boolean containsKey(TreeNode node, K key) {
        if (node == null) {
            return false;
        }
        if (key.compareTo(node.key) == 0) {
            return true;
        } else if (key.compareTo(node.key) < 0) {
            return containsKey(node.left, key);
        } else return containsKey(node.right, key);
    }

    @Override
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private V get(TreeNode node, K key) {
        if (node == null) {
            return null;
        }
        if (key.compareTo(node.key) == 0) {
            return node.value;
        } else if (key.compareTo(node.key) < 0) {
            return get(node.left, key);
        } else return get(node.right, key);
    }

    @Override
    public V get(K key) {
        return get(root, key);
    }

    @Override
    public int size() {
        return size(root);
    }

    private TreeNode put(TreeNode node, K key, V value) {
        if (node == null) {
            return new TreeNode(key, value);
        }
        if (key.compareTo(node.key) == 0) {
            node.value = value;
            return node;
        } else if (key.compareTo(node.key) < 0) {
            node.left = put(node.left, key, value);
        } else {
            node.right = put(node.right, key, value);
        }
        node.size = size(node.left) + size(node.right) + 1;
        return node;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
    }

    // 未实现的方法（Lab 8 或 Bonus 才需要）
    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (K k : this) {  // this 作为可迭代对象
            set.add(k);
        }
        return set;
    }

    public void printInOrder() {
        for (K k : this) {
            System.out.print(k + " ");
        }
        System.out.println();
    }

    private TreeNode findMax(TreeNode node) {
        if (node == null) {
            return null;
        }
        while (node.right != null) {
            node = node.right;
        }
        return node;
    }

    private TreeNode remove(TreeNode node, K key) {
        if (node == null) {
            return null;
        }
        if (key.compareTo(node.key) == 0) {
            removeValue = node.value;
            if (node.left == null) {
                return node.right;
            }
            if (node.right == null) {
                return node.left;
            }
            TreeNode maxNode = findMax(node.left);
            node.key = maxNode.key;
            node.value = maxNode.value;
            node.left = remove(node.left, maxNode.key);
        } else if (key.compareTo(node.key) < 0) {
            node.left = remove(node.left, key);
        } else {
            node.right = remove(node.right, key);
        }
        node.size = size(node.left) + size(node.right) + 1;
        return node;
    }

    @Override
    public V remove(K key) {
        removeValue = null;
        root = remove(root, key);
        return removeValue;
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return new BSTMapIter();
    }

    private class TreeNode {
        K key;
        V value;
        int size;
        TreeNode left, right;

        TreeNode(K k, V v) {
            key = k;
            value = v;
            size = 1;
        }
    }

    /**
     * An iterator that iterates over the keys of the dictionary.
     */
    private class BSTMapIter implements Iterator<K> {

        /**
         * Stores the current key-value pair.
         */
        private final Stack<TreeNode> nodeStack;

        /**
         * Create a new ULLMapIter by setting cur to the first node in the
         * linked list that stores the key-value pairs.
         */
        public BSTMapIter() {
            nodeStack = new Stack<>();
            pushLeft(root);
        }

        @Override
        public boolean hasNext() {
            return !nodeStack.isEmpty();
        }

        private void pushLeft(TreeNode node) {
            while (node != null) {
                nodeStack.push(node);
                node = node.left;
            }
        }

        @Override
        public K next() {
            TreeNode node = nodeStack.pop();
            pushLeft(node.right);
            return node.key;
        }

    }
}
