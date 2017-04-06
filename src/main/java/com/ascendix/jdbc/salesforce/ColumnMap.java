package com.ascendix.jdbc.salesforce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ColumnMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = -4362348575904029532L;
    
    private List<K> columnNames = new ArrayList<K>();
	private int columnPostion;
	
	@Override
	public V put(K key, V value) {
		columnNames.add(columnPostion++, key);
		return super.put(key, value);
	};
	
	/**
	 * Get a column name by index, starting at 1, that represents the insertion order into the map.
	 */
	public V getByIndex(int index) {
		return get(columnNames.get(index - 1));
	}
}
