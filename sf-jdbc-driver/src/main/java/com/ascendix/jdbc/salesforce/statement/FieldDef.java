package com.ascendix.jdbc.salesforce.statement;

public class FieldDef {

    /** Name of the field (or Name of the field used in aggregation) */
    private String name;
    /** Full bname of the field with sub entity or name of the aggregation function like:
     * 1) Owner.Name  for select Owner.Name from Account
     * 2) maxLastName for MAX(LastName)*/
    private String alias;
    private String type;

    public FieldDef(String name, String alias, String type) {
        this.name = name;
        this.alias = alias;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getAlias() {
        return alias;
    }

    public String getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        FieldDef other = (FieldDef) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
