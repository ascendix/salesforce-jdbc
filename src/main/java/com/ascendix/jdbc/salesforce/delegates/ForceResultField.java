package com.ascendix.jdbc.salesforce.delegates;

public class ForceResultField {

    public final static String NESTED_RESULT_SET_FIELD_TYPE = "nestedResultSet";

    private String entityType;
    private String name;
    private Object value;
    private String fieldType;

    public ForceResultField(String entityType, String fieldType, String name, Object value) {

        super();
        this.entityType = entityType;
        this.name = name;
        this.value = value;
        this.fieldType = fieldType;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public String getFullName() {
        return entityType != null ? entityType + "." + name : name;
    }

    @Override
    public String toString() {
        return "SfResultField [entityType=" + entityType + ", name=" + name + ", value=" + value + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        ForceResultField other = (ForceResultField) obj;
        if (entityType == null) {
            if (other.entityType != null)
                return false;
        } else if (!entityType.equals(other.entityType))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}