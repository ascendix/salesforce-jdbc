package com.ascendix.jdbc.salesforce.delegates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ForceQueryResult {

    public static class ForceResultFieldMetadata {
	private String entityType;
	private String fieldName;
	private String fieldType;

	public ForceResultFieldMetadata(String entityName, String fieldType, String fieldName) {
	    super();
	    this.entityType = entityName;
	    this.fieldName = fieldName;
	    this.fieldType = fieldType;
	}

	public String getEntityName() {
	    return entityType;
	}

	public String getFieldName() {
	    return fieldName;
	}
	
	public String getFieldType() {
	    return fieldType;
	}
	
	public String getFullName() {
	    return entityType != null ? entityType + "." + fieldName : fieldName;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
	    result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    ForceResultFieldMetadata other = (ForceResultFieldMetadata) obj;
	    if (entityType == null) {
		if (other.entityType != null)
		    return false;
	    } else if (!entityType.equals(other.entityType))
		return false;
	    if (fieldName == null) {
		if (other.fieldName != null)
		    return false;
	    } else if (!fieldName.equals(other.fieldName))
		return false;
	    return true;
	}
    }

    private List<ForceResultFieldMetadata> fields;
    private List<List<ForceResultField>> records = new LinkedList<>();

    public List<List<ForceResultField>> getRecords() {
	return Collections.unmodifiableList(records);
    }

    public synchronized void addRecord(List<ForceResultField> record) {
	records.add(record);
	if (fields == null) {
	    fields = new ArrayList<>();
	    record.stream()
	    		.forEach(field -> fields.add(new ForceResultFieldMetadata(field.getEntityType(), field.getFieldType(), field.getName())));
	}
    }

    public List<ForceResultFieldMetadata> getFields() {
        return fields;
    }

}