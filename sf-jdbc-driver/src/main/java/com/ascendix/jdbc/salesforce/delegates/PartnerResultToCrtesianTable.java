package com.ascendix.jdbc.salesforce.delegates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PartnerResultToCrtesianTable {

    private List<Object> schema;

    private PartnerResultToCrtesianTable(List schema) {
        this.schema = schema;
    }

    public static List<List> expand(List<List> list, List schema) {
        PartnerResultToCrtesianTable expander = new PartnerResultToCrtesianTable(schema);
        return expander.expandOn(list, 0, 0);
    }

    private List<List> expandOn(List<List> rows, int columnPosition, int schemaPosititon) {
        return rows.stream()
                .map(row -> expandRow(row, columnPosition, schemaPosititon))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<List> expandRow(List row, int columnPosition, int schemaPosititon) {
        List<List> result = new ArrayList<>();
        if (schemaPosititon > schema.size() - 1) {
            result.add(row);
            return result;
        } else if (schema.get(schemaPosititon) instanceof List) {
            int nestedListSize = ((List) schema.get(schemaPosititon)).size();
            Object value = row.get(columnPosition);
            List nestedList = value instanceof List ? (List) value : Collections.emptyList();
            if (nestedList.isEmpty()) {
                result.add(expandRow(row, Collections.nCopies(nestedListSize, null), columnPosition));
            } else {
                nestedList.forEach(item -> result.add(expandRow(row, item, columnPosition)));
            }
            return expandOn(result, columnPosition + nestedListSize, schemaPosititon + 1);
        } else {
            result.add(row);
            return expandOn(result, columnPosition + 1, schemaPosititon + 1);
        }

    }

    private static List expandRow(List row, Object nestedItem, int position) {
        List nestedItemsToInsert = nestedItem instanceof List ? (List) nestedItem : Arrays.asList(nestedItem);
        List newRow = new ArrayList<>(row.subList(0, position));
        newRow.addAll(nestedItemsToInsert);
        newRow.addAll(row.subList(position + 1, row.size()));
        return newRow;
    }
}
