package com.ascendix.jdbc.salesforce.statement.processor.utils;

import com.ascendix.jdbc.salesforce.statement.processor.UpdateQueryAnalyzerTest;

import java.util.HashMap;
import java.util.Map;

public class RecordFieldsBuilder {

    Map<String, Object> record = new HashMap<>();

    public static RecordFieldsBuilder setId(String id) {
        return new RecordFieldsBuilder().set("Id", id);
    }

    public static Map<String, Object> id(String id) {
        return new RecordFieldsBuilder().set("Id", id).build();
    }

    public RecordFieldsBuilder set(String field, Object value) {
        record.put(field, value);
        return this;
    }

    public Map<String, Object> build() {
        return record;
    }

}
