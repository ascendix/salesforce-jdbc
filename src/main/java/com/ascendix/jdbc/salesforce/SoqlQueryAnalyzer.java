package com.ascendix.jdbc.salesforce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.mule.tools.soql.SOQLDataBaseVisitor;
import org.mule.tools.soql.SOQLParserHelper;
import org.mule.tools.soql.query.SOQLQuery;
import org.mule.tools.soql.query.SOQLSubQuery;
import org.mule.tools.soql.query.clause.FromClause;
import org.mule.tools.soql.query.from.ObjectSpec;
import org.mule.tools.soql.query.select.FieldSpec;
import org.mule.tools.soql.query.select.FunctionCallSpec;

import com.sforce.soap.partner.ChildRelationship;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;

public class SoqlQueryAnalyzer {
    
    public abstract class AbstractFieldDef {}
    
    public class FieldDef extends AbstractFieldDef {
	
	private String name;
	private String type;
	
	public FieldDef(String name, String type) {
	    this.name = name;
	    this.type = type;
	}

	public String getName() {
	    return name;
	}

	public String getType() {
	    return type;
	}

    }
    
    public class SubqueryFieldsDef extends AbstractFieldDef {
	
	private List<AbstractFieldDef> fieldDefs;

	public SubqueryFieldsDef(List<AbstractFieldDef> fieldDefs) {
	    this.fieldDefs = fieldDefs;
	}

	public List<AbstractFieldDef> getFieldDefs() {
	    return fieldDefs;
	}

    }
    
    private String soql;
    private Function<String, DescribeSObjectResult> objectDescriptor;
    private Map<String, DescribeSObjectResult> describedObjectsCache;
    private SOQLQuery queryData;
    
    public SoqlQueryAnalyzer(String soql, Function<String, DescribeSObjectResult> objectDescriptor) {
	this(soql, objectDescriptor, new HashMap<>());
    }

    public SoqlQueryAnalyzer(String soql, Function<String, DescribeSObjectResult> objectDescriptor, Map<String, DescribeSObjectResult> describedObjectsCache) {
	this.soql = soql;
	this.objectDescriptor = objectDescriptor;
	this.describedObjectsCache = describedObjectsCache;
    }

    private List<AbstractFieldDef> fieldDefinitions;
    
    private class SelectSpecVisitor extends SOQLDataBaseVisitor<Void> {

	@Override
	public Void visitFieldSpec(FieldSpec fieldSpec) {
	    String name = fieldSpec.getFieldName();
	    String alias = fieldSpec.getAlias() != null ? fieldSpec.getAlias() : name;
	    List<String> prefixNames = new ArrayList<String>(fieldSpec.getObjectPrefixNames());
	    FieldDef result = createFieldDef(name, alias, prefixNames);
	    fieldDefinitions.add(result);
	    return null;
	}

	private FieldDef createFieldDef(String name, String alias, List<String> prefixNames) {
	    List<String> fieldPrefixes = new ArrayList<>(prefixNames);
	    String fromObject = getFromObjectName();
	    if (! fieldPrefixes.isEmpty() && fieldPrefixes.get(0).equalsIgnoreCase(fromObject)) {
		fieldPrefixes.remove(0);
	    }
	    while (! fieldPrefixes.isEmpty()) {
		String referenceName = fieldPrefixes.get(0);
		Field reference = findField(referenceName, describeObject(fromObject), fld -> fld.getRelationshipName());
		fromObject = reference.getReferenceTo()[0];
		fieldPrefixes.remove(0);
	    }
	    String type = findField(name, describeObject(fromObject), fld -> fld.getName()).getType().name();
	    FieldDef result = new FieldDef(alias, type);
	    return result;
	}

	private final List<String> FUNCTIONS_HAS_INT_RESULT = Arrays.asList("count", "count_distinct");
	
	@Override
	public Void visitFunctionCallSpec(FunctionCallSpec functionCallSpec) {
	    String alias = functionCallSpec.getAlias() != null ? functionCallSpec.getAlias() : functionCallSpec.getFunctionName();
	    if (FUNCTIONS_HAS_INT_RESULT.contains(functionCallSpec.getFunctionName().toLowerCase())) {
		fieldDefinitions.add(new FieldDef(alias, "int"));
	    } else {
		org.mule.tools.soql.query.data.Field param = (org.mule.tools.soql.query.data.Field) functionCallSpec.getFunctionParameters().get(0);
		FieldDef result = createFieldDef(param.getFieldName(), alias, param.getObjectPrefixNames());
		fieldDefinitions.add(result);
	    }
	    return null;
	}

	@Override
	public Void visitSOQLSubQuery(SOQLSubQuery soqlSubQuery) {
	    String subquerySoql = soqlSubQuery.toSOQLText().replaceAll("\\A\\s*\\(|\\)\\s*$", "");
	    SOQLQuery subquery = SOQLParserHelper.createSOQLData(subquerySoql);
	    String relationshipName = subquery.getFromClause().getMainObjectSpec().getObjectName();
	    ChildRelationship relatedFrom = Arrays.stream(describeObject(getFromObjectName()).getChildRelationships())
		    .filter(rel -> relationshipName.equalsIgnoreCase(rel.getRelationshipName()))
		    .findFirst()
		    .orElseThrow(() -> new IllegalArgumentException("Unresolved relationship in subquery \"" + subquerySoql + "\""));
	    String fromObject = relatedFrom.getChildSObject();
	    subquery.setFromClause(new FromClause(new ObjectSpec(fromObject, null)));
	    
	    SoqlQueryAnalyzer subqueryAnalyzer = new SoqlQueryAnalyzer(subquery.toSOQLText(), objectDescriptor, describedObjectsCache);
	    fieldDefinitions.add(new SubqueryFieldsDef(subqueryAnalyzer.getFieldDefinitions()));
	    return null;
	}
	
    }
    
    public List<AbstractFieldDef> getFieldDefinitions() {
	if (fieldDefinitions == null) {
	    fieldDefinitions = new ArrayList<>();
	    SelectSpecVisitor visitor = new SelectSpecVisitor();
	    getQueryData().getSelectSpecs().stream()
		    .forEach(spec -> spec.accept(visitor));
	}
	return fieldDefinitions;
    }

    private Field findField(String name, DescribeSObjectResult objectDesc, Function<Field, String> nameFetcher) {
	    return Arrays.stream(objectDesc.getFields())
		    .filter(field -> name.equals(nameFetcher.apply(field)))
		    .findFirst()
		    .orElseThrow(() -> new IllegalArgumentException("Unknown field name \"" + name + "\" in object \"" + objectDesc.getName() + "\""));
    }
    
    private DescribeSObjectResult describeObject(String fromObjectName) {
	if (! describedObjectsCache.containsKey(fromObjectName)) {
	    DescribeSObjectResult description = objectDescriptor.apply(fromObjectName);
	    describedObjectsCache.put(fromObjectName, description);
	    return description;
	} else {
	    return (DescribeSObjectResult) describedObjectsCache.get(fromObjectName);
	}
    }

    protected String getFromObjectName() {
	return getQueryData().getFromClause().getMainObjectSpec().getObjectName();
    }
    
    private SOQLQuery getQueryData() {
	if (queryData == null) {
	    queryData = SOQLParserHelper.createSOQLData(soql);
	}
	return queryData;
    }
    
}
