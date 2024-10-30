package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 * <p>
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author Moiads
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
    private int size;
    private int initialSize;
    private double loadFactor;
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        initialSize = 16;
        loadFactor = 0.75;
        buckets = createTable(initialSize);
    }

    public MyHashMap(int initialSize) {
        this.initialSize = initialSize;
        loadFactor = 0.75;
        buckets = createTable(initialSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of a backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.initialSize = initialSize;
        this.loadFactor = maxLoad;
        buckets = createTable(initialSize);
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
        return new LinkedList<>();
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
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    private Collection[] resize(int newSize) {
        Collection<Node>[] buckets = createTable(newSize);
        for (Collection<Node> bucket : this.buckets) {
            if (bucket != null) {
                for (Node node : bucket) {
                    put(buckets, node.key, node.value);
                }
            }
        }
        return buckets;
    }

    @Override
    public void clear() {
        Arrays.fill(buckets, null);
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] != null) {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] != null) {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    return node.value;
                }
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private void put(Collection[] buckets, K key, V value) {
        Node newNode = createNode(key, value);
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            buckets[index] = createBucket();
        }
        buckets[index].add(newNode);
    }

    @Override
    public void put(K key, V value) {
        Node newNode = createNode(key, value);
        int index = Math.floorMod(key.hashCode(), buckets.length);
        if (buckets[index] == null) {
            buckets[index] = createBucket();
        }
        if (containsKey(key)) {
            for (Node node : buckets[index]) {
                if (node.key.equals(key)) {
                    node.value = value;
                }
            }
        } else {
            size++;
        }
        buckets[index].add(newNode);
        if ((size / (double) buckets.length) > loadFactor) {
            buckets = resize(buckets.length * 2);
        }
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            if (bucket != null) {
                for (Node node : bucket) {
                    keys.add(node.key);
                }
            }
        }
        return keys;
    }

    @Override
    public V remove(K key) throws UnsupportedOperationException {
        return null;
    }

    @Override
    public V remove(K key, V value) throws UnsupportedOperationException {
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        Set<K> keys = keySet();
        return keys.iterator();
    }

}
