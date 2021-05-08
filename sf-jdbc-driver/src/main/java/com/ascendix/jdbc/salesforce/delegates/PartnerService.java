package com.ascendix.jdbc.salesforce.delegates;

import com.ascendix.jdbc.salesforce.metadata.Column;
import com.ascendix.jdbc.salesforce.metadata.Table;
import com.ascendix.jdbc.salesforce.statement.FieldDef;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;
import org.apache.commons.collections4.IteratorUtils;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PartnerService {

    private static final String SF_JDBC_DRIVER_NAME = "SF JDBC driver";
    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    private PartnerConnection partnerConnection;
    private List<String> sObjectTypesCache;

    public PartnerService(PartnerConnection partnerConnection) {
        this.partnerConnection = partnerConnection;
    }

    public List<Table> getTables() {
        logger.info("[PartnerService] getTables IMPLEMENTED ");
        List<DescribeSObjectResult> sObjects = getSObjectsDescription();
        List<Table> tables = sObjects.stream()
                .map(this::convertToTable)
                .collect(Collectors.toList());
        logger.info("[PartnerService] getTables tables count="+tables.size());
        return tables;
    }

    public DescribeSObjectResult describeSObject(String sObjectType) throws ConnectionException {
        logger.info("[PartnerService] describeSObject "+sObjectType);
        return partnerConnection.describeSObject(sObjectType);
    }

    private Table convertToTable(DescribeSObjectResult so) {
        logger.info("[PartnerService] convertToTable "+so.getName());
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
            logger.info("[PartnerService] getSObjectTypes count="+sObjectTypesCache.size());
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
        logger.info("[PartnerService] query "+soql);
        List<List> resultRows = Collections.synchronizedList(new LinkedList<>());
        QueryResult queryResult = null;
        do {
            queryResult = queryResult == null ? partnerConnection.query(soql)
                    : partnerConnection.queryMore(queryResult.getQueryLocator());

            List<XmlObject> rows = Arrays.asList(queryResult.getRecords());
            // extract the root entity name
            Object rootEntityName = rows.stream().filter(xmlo -> "type".equals(xmlo.getName().getLocalPart())).findFirst().map(XmlObject::getValue).orElse(null);
            String parentName = null;
            resultRows.addAll(removeServiceInfo(rows, parentName, rootEntityName==null ? null : (String)rootEntityName));
        } while (!queryResult.isDone());

        return PartnerResultToCrtesianTable.expand(resultRows, expectedSchema);
    }

    private List<List> removeServiceInfo(List<XmlObject> rows, String parentName, String rootEntityName) {
        return rows.stream()
                .filter(this::isDataObjectType)
                .map(row -> removeServiceInfo(row, parentName, rootEntityName))
                .collect(Collectors.toList());
    }

    private List<ForceResultField> removeServiceInfo(XmlObject row, String parentName, String rootEntityName) {
        return IteratorUtils.toList(row.getChildren()).stream()
                .filter(this::isDataObjectType)
                .skip(1) // Removes duplicate Id from SF Partner API response
                // (https://developer.salesforce.com/forums/?id=906F00000008kciIAA)
                .flatMap(field -> translateField(field, parentName, rootEntityName))
                .collect(Collectors.toList());
    }

    private Stream<ForceResultField> translateField(XmlObject field, String parentName, String rootEntityName) {
        Stream.Builder outStream = Stream.builder();

        String fieldType = field.getXmlType() != null ? field.getXmlType().getLocalPart() : null;
        if ("sObject".equalsIgnoreCase(fieldType)) {
            List<ForceResultField> childFields = removeServiceInfo(field, field.getName().getLocalPart(), rootEntityName);
            childFields.forEach(outStream::add);
        } else {
            if (isNestedResultset(field)) {
                outStream.add(removeServiceInfo(IteratorUtils.toList(field.getChildren()), field.getName().getLocalPart(), rootEntityName));
            } else {
                outStream.add(toForceResultField(field, parentName, rootEntityName));
            }
        }
        return outStream.build();
    }


    private ForceResultField toForceResultField(XmlObject field, String parentName, String rootEntityName) {
        String fieldType = field.getXmlType() != null ? field.getXmlType().getLocalPart() : null;
        if ("sObject".equalsIgnoreCase(fieldType)) {
            List<XmlObject> children = new ArrayList<>();
            field.getChildren().forEachRemaining(children::add);
            field = children.get(2);
        }
        String name = field.getName().getLocalPart();
        if (parentName != null && (rootEntityName == null || !rootEntityName.equals(parentName))) {
            name = parentName+"."+name;
        }
        Object value = field.getValue();
        return new ForceResultField(null, fieldType, name, value);
    }

    private boolean isNestedResultset(XmlObject object) {
        return object.getXmlType() != null && "QueryResult".equals(object.getXmlType().getLocalPart());
    }

    private final static List<String> SOAP_RESPONSE_SERVICE_OBJECT_TYPES = Arrays.asList("type", "done", "queryLocator",
            "size");

    private boolean isDataObjectType(XmlObject obj) {
        return !SOAP_RESPONSE_SERVICE_OBJECT_TYPES.contains(obj.getName().getLocalPart());
    }

    public SaveResult[] createRecords(String entityName, List<Map<String, Object>> recordsDefinitions) throws ConnectionException {
        // Create a new sObject of type Contact
        // and fill out its fields.

        SObject[] records = new SObject[recordsDefinitions.size()];


        for (int i = 0; i < recordsDefinitions.size(); i++) {
            Map<String, Object> recordDef = recordsDefinitions.get(i);
            SObject record = records[i] = new SObject();
            record.setType(entityName);
            for (Map.Entry<String, Object> field: recordDef.entrySet()) {
                record.setField(field.getKey(), field.getValue());
            }
        }
        // Make a create call and pass it the array of sObjects
        SaveResult[] results = partnerConnection.create(records);
        return results;
    }

    public SaveResult[] saveRecords(String entityName, List<Map<String, Object>> recordsDefinitions) throws ConnectionException {
        // Create a new sObject of type Contact
        // and fill out its fields.

        SObject[] records = new SObject[recordsDefinitions.size()];


        for (int i = 0; i < recordsDefinitions.size(); i++) {
            Map<String, Object> recordDef = recordsDefinitions.get(i);
            SObject record = records[i] = new SObject();
            record.setType(entityName);
            for (Map.Entry<String, Object> field: recordDef.entrySet()) {
                record.setField(field.getKey(), field.getValue());
            }
        }
        // Make a create call and pass it the array of sObjects
        SaveResult[] results = partnerConnection.update(records);
        return results;
    }
}
