package bstmap;

import java.util.Iterator;
import java.util.Set;

/**
 * @author Moiads
 */
//order by key
public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private int size;

    @Override
    public void clear() {
        rootNode = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return containsKeyHelper(rootNode, key);
    }

    //if put("a", null) , this method is helpful
    private boolean containsKeyHelper(Entry node, K key) {
        if (node == null) {
            return false;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return containsKeyHelper(node.left, key);
        } else if (cmp > 0) {
            return containsKeyHelper(node.right, key);
        } else {
            return true;
        }
    }

    @Override
    public V get(K key) {
        return get(rootNode, key);
    }

    private V get(Entry root, K key) {
        if (root == null) {
            return null;
        } else {
            int cmp = key.compareTo(root.key);
            if (cmp == 0) {
                return root.value;
            } else if (cmp < 0) {
                return get(root.left, key);
            } else {
                return get(root.right, key);
            }
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        if (rootNode == null) {
            rootNode = new Entry(key, value);
        } else {
            rootNode.put(rootNode, key, value);
        }
        size++;
    }

    //store (k,y) left child node and right child node
    private Entry rootNode;

    private class Entry {

        K key;
        V value;
        Entry left, right;

        private Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        private void put(Entry root, K key, V value) {
            int compare = key.compareTo(root.key);
            if (compare < 0) {
                if (root.left == null) {
                    root.left = new Entry(key, value);
                } else {
                    put(root.left, key, value);
                }
            } else if (compare == 0) {
                root.value = value;
            } else {
                if (root.right == null) {
                    root.right = new Entry(key, value);
                } else {
                    put(root.right, key, value);
                }
            }
        }
    }

    public void printInOrder() {
        printIn(this.rootNode);
    }
    private void printIn(Entry root){
        if (root == null) {
            return;
        }
        printIn(root.left);
        System.out.println(root.value);
        printIn(root.right);
    }



    //we do not need to implement these.
    @Override
    public Set<K> keySet() throws UnsupportedOperationException {
        return Set.of();
    }

    @Override
    public V remove(K key) throws UnsupportedOperationException {
        return null;
    }

    @Override
    public V remove(K key, V value) throws UnsupportedOperationException {
        return null;
    }

    @SuppressWarnings({"NullableProblems", "DataFlowIssue"})
    @Override
    public Iterator<K> iterator() throws UnsupportedOperationException{
        return null;

    }
}
