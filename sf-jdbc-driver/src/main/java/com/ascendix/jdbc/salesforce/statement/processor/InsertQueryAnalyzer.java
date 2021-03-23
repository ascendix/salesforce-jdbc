package com.ascendix.jdbc.salesforce.statement.processor;

import com.ascendix.jdbc.salesforce.statement.processor.utils.ValueToStringVisitor;
import com.sforce.soap.partner.DescribeSObjectResult;
import com.sforce.soap.partner.Field;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ascendix.jdbc.salesforce.statement.processor.InsertQueryProcessor.SF_JDBC_DRIVER_NAME;

public class InsertQueryAnalyzer {

    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    private String soql;
    private Function<String, DescribeSObjectResult> objectDescriptor;
    private Map<String, DescribeSObjectResult> describedObjectsCache;
    private Function<String, List<Map<String, Object>>> subSelectResolver;
    private Insert queryData;
    private List<Map<String, Object>> records;

    public InsertQueryAnalyzer(String soql, Function<String, DescribeSObjectResult> objectDescriptor) {
        this(soql, objectDescriptor, new HashMap<>(), null);
    }

    public InsertQueryAnalyzer(String soql,
                               Function<String, DescribeSObjectResult> objectDescriptor,
                               Map<String, DescribeSObjectResult> describedObjectsCache,
                               Function<String, List<Map<String, Object>>> subSelectResolver) {
        this.soql = soql;
        this.objectDescriptor = objectDescriptor;
        this.describedObjectsCache = describedObjectsCache;
        this.subSelectResolver = subSelectResolver;
    }

    public boolean analyse(String soql) {
        if (soql == null || soql.trim().length() == 0) {
            return false;
        }
        this.soql = soql;
        return getQueryData() != null;
    }

    public class InsertItemsListVisitor implements ItemsListVisitor {
        List<Column> columns;
        List<Map<String, Object>> records;

        public InsertItemsListVisitor(List<Column> columns, List<Map<String, Object>> records) {
            this.columns = columns;
            this.records = records;
        }

        @Override
        public void visit(SubSelect subSelect) {
            System.out.println("SubSelect Visitor");
        }

        @Override
        public void visit(ExpressionList expressionList) {
            System.out.println("Expression Visitor");
            HashMap<String, Object> fieldValues = new HashMap<>();
            records.add(fieldValues);

            for(int i = 0; i < columns.size(); i++) {
                expressionList.getExpressions().get(i).accept(
                        new ValueToStringVisitor(
                                fieldValues,
                                columns.get(i).getColumnName(),
                                subSelectResolver)
                );
            }
        }

        @Override
        public void visit(NamedExpressionList namedExpressionList) {
            System.out.println("NamedExpression Visitor");
        }

        @Override
        public void visit(MultiExpressionList multiExprList) {
            System.out.println("MultiExpression Visitor");
            multiExprList.getExpressionLists().forEach(expressions -> {
                expressions.accept(new InsertItemsListVisitor(columns, records));
            });
        }
    }

    private Field findField(String name, DescribeSObjectResult objectDesc, Function<Field, String> nameFetcher) {
        return Arrays.stream(objectDesc.getFields())
                .filter(field -> name.equalsIgnoreCase(nameFetcher.apply(field)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown field name \"" + name + "\" in object \"" + objectDesc.getName() + "\""));
    }

    private DescribeSObjectResult describeObject(String fromObjectName) {
        if (!describedObjectsCache.containsKey(fromObjectName)) {
            DescribeSObjectResult description = objectDescriptor.apply(fromObjectName);
            describedObjectsCache.put(fromObjectName, description);
            return description;
        } else {
            return describedObjectsCache.get(fromObjectName);
        }
    }

    protected String getFromObjectName() {
        return queryData.getTable().getName();
    }

    private Insert getQueryData() {
        if (queryData == null) {
            try {
                Statement statement = CCJSqlParserUtil.parse(soql);
                if (statement instanceof Insert) {
                    queryData = (Insert) statement;
                }
            } catch (JSQLParserException e) {
                logger.log(Level.SEVERE,"Failed request to create entities with error: "+e.getMessage(), e);
            }
        }
        return queryData;
    }

    public List<Map<String, Object>> getRecords() {
        if (queryData != null && records == null) {
            records = new ArrayList<>();
            if (getQueryData().isUseValues()) {
                getQueryData().getItemsList().accept(new InsertItemsListVisitor(getQueryData().getColumns(), records));
            } else {
                if (getQueryData().getSelect() != null) {
                    if (subSelectResolver != null) {
                        List<Map<String, Object>> subRecords = subSelectResolver.apply(getQueryData().getSelect().toString());

                        for (Map<String, Object> subRecord: subRecords) {
                            // this subRecord is LinkedHashMap - so the order of fields is determined by soql
                            Map<String, Object> record = new HashMap<>();
                            records.add(record);
                            int fieldIndex = 0;
                            Iterator<Map.Entry<String, Object>> fieldsIterator = subRecord.entrySet().iterator();
                            while(fieldsIterator.hasNext()) {
                                Map.Entry<String, Object> fieldEntry = fieldsIterator.next();
                                String insertFieldName = queryData.getColumns().get(fieldIndex).getColumnName();
                                String subSelectFieldName = fieldEntry.getKey();
                                Object subSelectFieldValue = fieldEntry.getValue();
                                record.put(insertFieldName, subSelectFieldValue);

                                fieldIndex ++;
                            }
                        }

                    }
                }
            }
        }
        return records;
    }
}
