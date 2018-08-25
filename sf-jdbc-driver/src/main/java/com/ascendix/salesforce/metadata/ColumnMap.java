package com.ascendix.salesforce.metadata;

import java.io.Serializable;
import java.util.ArrayList;

public class ColumnMap<K, V> implements Serializable {

    private static final long serialVersionUID = 2705233366870541749L;

    private ArrayList<K> columnNames = new ArrayList<>();
    private ArrayList<V> values = new ArrayList<>();
    private int columnPosition = 0;

    public V put(K key, V value) {
        columnNames.add(columnPosition, key);
        values.add(columnPosition, value);
        columnPosition++;
        return value;
    }

    public V get(K key) {
        int index = columnNames.indexOf(key);
        return index != -1 ? values.get(index) : null;
    }

    /**
     * Get a column name by index, starting at 1, that represents the insertion
     * order into the map.
     */
    public V getByIndex(int index) {
        return values.get(index - 1);
    }
}
