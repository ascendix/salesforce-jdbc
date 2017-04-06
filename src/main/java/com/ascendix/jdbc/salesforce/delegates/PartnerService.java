package com.ascendix.jdbc.salesforce.delegates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.ascendix.jdbc.salesforce.Column;
import com.ascendix.jdbc.salesforce.Table;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.DescribeGlobalSObjectResult;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;

public class PartnerService {

    private PartnerConnection partnerConnection;
    private List<String> sObjectTypesCache;

    public PartnerService(PartnerConnection partnerConnection) {
	this.partnerConnection = partnerConnection;
    }

    public List<Table> getTables() {
	List<DescribeSObjectResult> sObjects = getSObjectsDescription();
	return sObjects.stream().parallel()
		.map(so -> convertToTable(so))
		.collect(Collectors.toList());
    }
    
    public DescribeSObjectResult describeSObject(String sObjectType) throws ConnectionException {
	return partnerConnection.describeSObject(sObjectType);
    }

    private Table convertToTable(DescribeSObjectResult so) {
	List<Field> fields = Arrays.asList(so.getFields());
	List<Column> columns = fields.stream()
		.map(f -> convertToColumn(f))
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
	    sObjectTypesCache = Arrays.stream(sobs).parallel()
		    .map(sob -> sob.getName())
		    .collect(Collectors.toList());
	}
	return sObjectTypesCache;

    }

    private List<DescribeSObjectResult> getSObjectsDescription() {
	DescribeGlobalResult describeGlobals = describeGlobal();
	List<String> tableNames = Arrays.stream(describeGlobals.getSobjects()).parallel()
		.map(so -> so.getName())
		.collect(Collectors.toList());
	List<List<String>> tableNamesBatched = toBatches(tableNames, 100);
	return tableNamesBatched.stream().parallel()
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

    public ForceQueryResult query(String soql) throws ConnectionException {
	ForceQueryResult result = new ForceQueryResult();
	QueryResult queryResult = null;
	do {
	    queryResult = queryResult == null
		    ? partnerConnection.query(soql)
		    : partnerConnection.queryMore(queryResult.getQueryLocator());
	    Arrays.stream(queryResult.getRecords()).parallel()
			.forEach(record -> result.addRecord(toRecord(record)));
	} while (! queryResult.isDone());
	return result;
    }

    private List<ForceResultField> toRecord(XmlObject record) {
	Set<ForceResultField> result = new LinkedHashSet<>();
	toStream(record.getChildren())
		.filter(this::acceptableObjectType)
		.forEach(field -> {
		    if (isNestedResultset(field)) {
			List<List<ForceResultField>> nestedResult = toStream(field.getChildren())
				.filter(f -> f instanceof SObject)
				.map(f -> toRecord(f))
				.collect(Collectors.toList());
			String entityType = getEntityType(field);
			ForceResultField resultSetField = new ForceResultField(entityType, ForceResultField.NESTED_RESULT_SET_FIELD_TYPE, entityType, nestedResult);
			result.add(resultSetField);
		    } else {
			String name = field.getName().getLocalPart();
			Object value = field.getValue();
			String fieldType = field.getXmlType() != null ? field.getXmlType().getLocalPart() : null;
			ForceResultField sfField = new ForceResultField(getEntityType(record), fieldType, name, value);
			result.add(sfField);
		    }
		});
	return new ArrayList<>(result);
    }

    private boolean isNestedResultset(XmlObject object) {
	return object.getXmlType() != null && "QueryResult".equals(object.getXmlType().getLocalPart());
    }
    
    private final static List<String> SKIP_OBJECT_TYPES = Arrays.asList("type", "done", "queryLocator", "size");
    
    private boolean acceptableObjectType(XmlObject object) {
	return ! SKIP_OBJECT_TYPES.contains(object.getName().getLocalPart());
    }
    
    private <T> Stream<T> toStream(Iterator<T> iterator) {
	return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
    }
    
    private String getEntityType(XmlObject record) {
	Iterator<XmlObject> fieldsIterator = record.getChildren();
	return toStream(fieldsIterator)
		.filter(field -> "type".equals(field.getName().getLocalPart()))
		.map(field -> (String) field.getValue())
		.findAny()
		.orElse(null);
    }

}
