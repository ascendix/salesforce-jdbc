package com.ascendix.jdbc.salesforce.delegates;

import com.ascendix.jdbc.salesforce.metadata.Column;
import com.ascendix.jdbc.salesforce.metadata.Table;
import com.ascendix.jdbc.salesforce.statement.FieldDef;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;
import org.apache.commons.collections4.IteratorUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PartnerService {

    private PartnerConnection partnerConnection;
    private List<String> sObjectTypesCache;

    public PartnerService(PartnerConnection partnerConnection) {
        this.partnerConnection = partnerConnection;
    }

    public List<Table> getTables() {
        List<DescribeSObjectResult> sObjects = getSObjectsDescription();
        return sObjects.stream()
                .map(this::convertToTable)
                .collect(Collectors.toList());
    }

    public DescribeSObjectResult describeSObject(String sObjectType) throws ConnectionException {
        return partnerConnection.describeSObject(sObjectType);
    }

    private Table convertToTable(DescribeSObjectResult so) {
        List<Field> fields = Arrays.asList(so.getFields());
        List<Column> columns = fields.stream()
                .map(this::convertToColumn)
                .collect(Collectors.toList());
        return new Table(so.getName(), null, columns);
    }

    private Column convertToColumn(Field field) {
        try {
            Column column = new Column(field.getName(), getType(field));
            column.setNillable(false);
            column.setCalculated(field.isCalculated() || field.isAutoNumber());
            String[] referenceTos = field.getReferenceTo();
            if (referenceTos != null) {
                for (String referenceTo : referenceTos) {
                    if (getSObjectTypes().contains(referenceTo)) {
                        column.setReferencedTable(referenceTo);
                        column.setReferencedColumn("Id");
                    }
                }
            }
            return column;
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private String getType(Field field) {
        String s = field.getType().toString();
        if (s.startsWith("_")) {
            s = s.substring("_".length());
        }
        return s.equalsIgnoreCase("double") ? "decimal" : s;
    }

    private List<String> getSObjectTypes() throws ConnectionException {
        if (sObjectTypesCache == null) {
            DescribeGlobalSObjectResult[] sobs = partnerConnection.describeGlobal().getSobjects();
            sObjectTypesCache = Arrays.stream(sobs)
                    .map(DescribeGlobalSObjectResult::getName)
                    .collect(Collectors.toList());
        }
        return sObjectTypesCache;

    }

    private List<DescribeSObjectResult> getSObjectsDescription() {
        DescribeGlobalResult describeGlobals = describeGlobal();
        List<String> tableNames = Arrays.stream(describeGlobals.getSobjects())
                .map(DescribeGlobalSObjectResult::getName)
                .collect(Collectors.toList());
        List<List<String>> tableNamesBatched = toBatches(tableNames, 100);
        return tableNamesBatched.stream()
                .flatMap(batch -> describeSObjects(batch).stream())
                .collect(Collectors.toList());
    }

    private DescribeGlobalResult describeGlobal() {
        try {
            return partnerConnection.describeGlobal();
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<DescribeSObjectResult> describeSObjects(List<String> batch) {
        DescribeSObjectResult[] result;
        try {
            result = partnerConnection.describeSObjects(batch.toArray(new String[0]));
            return Arrays.asList(result);
        } catch (ConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> List<List<T>> toBatches(List<T> objects, int batchSize) {
        List<List<T>> result = new ArrayList<>();
        for (int fromIndex = 0; fromIndex < objects.size(); fromIndex += batchSize) {
            int toIndex = Math.min(fromIndex + batchSize, objects.size());
            result.add(objects.subList(fromIndex, toIndex));
        }
        return result;
    }

    public List<List> query(String soql, List<FieldDef> expectedSchema) throws ConnectionException {
        List<List> resultRows = Collections.synchronizedList(new LinkedList<>());
        QueryResult queryResult = null;
        do {
            queryResult = queryResult == null ? partnerConnection.query(soql)
                    : partnerConnection.queryMore(queryResult.getQueryLocator());
            resultRows.addAll(removeServiceInfo(Arrays.asList(queryResult.getRecords())));
        } while (!queryResult.isDone());

        return PartnerResultToCrtesianTable.expand(resultRows, expectedSchema);
    }


    private List<List> removeServiceInfo(List<XmlObject> rows) {
        return rows.stream()
                .filter(this::isDataObjectType)
                .map(this::removeServiceInfo)
                .collect(Collectors.toList());
    }

    private List removeServiceInfo(XmlObject row) {
        return IteratorUtils.toList(row.getChildren()).stream()
                .filter(this::isDataObjectType)
                .skip(1) // Removes duplicate Id from SF Partner API response
                // (https://developer.salesforce.com/forums/?id=906F00000008kciIAA)
                .map(field -> processFieldObject(field))
                .collect(Collectors.toList());

    }


    @NotNull
    private ForceResultField getForceResultField(XmlObject field, String fieldType) {
        String name = field.getName().getLocalPart();
        Object value = field.getValue();
        return new ForceResultField(null, fieldType, name, value);
    }

    private ForceResultField processFieldObject(XmlObject xmlField) {
        String fieldType = xmlField.getXmlType() != null ? xmlField.getXmlType().getLocalPart() : null;
        if ("QueryResult".equals(fieldType)) {
             //removeServiceInfo(IteratorUtils.toList(xmlField.getChildren()));
            return null;
        } else if ("sObject".equals(fieldType)) {
            List children = new ArrayList<>();
            xmlField.getChildren().forEachRemaining(xmlObject -> children.add(getForceResultField(xmlObject,
                    fieldType)));
            return (ForceResultField)children.get(0);
        } else {
            return getForceResultField(xmlField, fieldType);
        }
    }

    private final static List<String> SOAP_RESPONSE_SERVICE_OBJECT_TYPES = Arrays.asList("type", "done", "queryLocator",
            "size");

    private boolean isDataObjectType(XmlObject object) {
        return !SOAP_RESPONSE_SERVICE_OBJECT_TYPES.contains(object.getName().getLocalPart());
    }
}
