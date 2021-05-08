package com.ascendix.jdbc.salesforce.statement.processor.utils;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.List;
import java.util.Map;

public class ValueToStringVisitor implements ExpressionVisitor {

    private Map<String, Object> fieldValues;
    private String columnName;
    private java.util.function.Function<String, List<Map<String, Object>>> subSelectResolver;

    public ValueToStringVisitor(Map<String, Object> fieldValues, String columnName, java.util.function.Function<String, List<Map<String, Object>>> subSelectResolver) {
        this.fieldValues = fieldValues;
        this.columnName = columnName;
        this.subSelectResolver = subSelectResolver;
    }

    @Override
    public void visit(BitwiseRightShift aThis) {
        System.out.println("[VtoSVisitor] BitwiseRightShift");
    }

    @Override
    public void visit(BitwiseLeftShift aThis) {
        System.out.println("[VtoSVisitor] BitwiseLeftShift");
    }

    @Override
    public void visit(NullValue nullValue) {
        System.out.println("[VtoSVisitor] NullValue");
        fieldValues.put(columnName, null);
    }

    @Override
    public void visit(Function function) {
        System.out.println("[VtoSVisitor] Function");
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        System.out.println("[VtoSVisitor] SignedExpression");
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        System.out.println("[VtoSVisitor] BitwiseRightShift");
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        System.out.println("[VtoSVisitor] JdbcNamedParameter");
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        System.out.println("[VtoSVisitor] DoubleValue="+doubleValue.getValue());
        fieldValues.put(columnName, doubleValue.getValue());
    }

    @Override
    public void visit(LongValue longValue) {
        System.out.println("[VtoSVisitor] LongValue="+longValue.getValue());
        fieldValues.put(columnName, longValue.getValue());
    }

    @Override
    public void visit(HexValue hexValue) {
        System.out.println("[VtoSVisitor] HexValue="+hexValue.getValue());
        fieldValues.put(columnName, hexValue.getValue());
    }

    @Override
    public void visit(DateValue dateValue) {
        System.out.println("[VtoSVisitor] DateValue");
    }

    @Override
    public void visit(TimeValue timeValue) {
        System.out.println("[VtoSVisitor] BitwiseRightShift");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        System.out.println("[VtoSVisitor] TimestampValue");
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        System.out.println("[VtoSVisitor] Parenthesis");
    }

    @Override
    public void visit(StringValue stringValue) {
        System.out.println("[VtoSVisitor] StringValue="+stringValue.getValue());
        fieldValues.put(columnName, stringValue.getValue());
    }

    @Override
    public void visit(Addition addition) {
        System.out.println("[VtoSVisitor] Addition");
    }

    @Override
    public void visit(Division division) {
        System.out.println("[VtoSVisitor] Division");

    }

    @Override
    public void visit(IntegerDivision division) {
        System.out.println("[VtoSVisitor] IntegerDivision");

    }

    @Override
    public void visit(Multiplication multiplication) {
        System.out.println("[VtoSVisitor] Multiplication");

    }

    @Override
    public void visit(Subtraction subtraction) {
        System.out.println("[VtoSVisitor] Subtraction");

    }

    @Override
    public void visit(AndExpression andExpression) {
        System.out.println("[VtoSVisitor] AndExpression");

    }

    @Override
    public void visit(OrExpression orExpression) {
        System.out.println("[VtoSVisitor] OrExpression");

    }

    @Override
    public void visit(Between between) {
        System.out.println("[VtoSVisitor] Between");

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        System.out.println("[VtoSVisitor] EqualsTo");

    }

    @Override
    public void visit(GreaterThan greaterThan) {
        System.out.println("[VtoSVisitor] GreaterThan");

    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        System.out.println("[VtoSVisitor] GreaterThanEquals");

    }

    @Override
    public void visit(InExpression inExpression) {
        System.out.println("[VtoSVisitor] InExpression");

    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        System.out.println("[VtoSVisitor] FullTextSearch");

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        System.out.println("[VtoSVisitor] IsNullExpression");

    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        System.out.println("[VtoSVisitor] IsBooleanExpression");

    }

    @Override
    public void visit(LikeExpression likeExpression) {
        System.out.println("[VtoSVisitor] LikeExpression");

    }

    @Override
    public void visit(MinorThan minorThan) {
        System.out.println("[VtoSVisitor] MinorThan");

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        System.out.println("[VtoSVisitor] MinorThanEquals");

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        System.out.println("[VtoSVisitor] NotEqualsTo");

    }

    @Override
    public void visit(Column tableColumn) {
        System.out.println("[VtoSVisitor] Column");

    }

    @Override
    public void visit(SubSelect subSelect) {
        System.out.println("[VtoxSVisitor] SubSelect="+subSelect.toString());
        Object value = null;
        if (subSelectResolver != null) {
            subSelect.setUseBrackets(false);
            List<Map<String, Object>> records = subSelectResolver.apply(subSelect.toString());
            if (records.size() == 1 && records.get(0).size() == 1) {
                // return the value as plain value
                value = records.get(0).entrySet().iterator().next().getValue();
                System.out.println("[VtoSVisitor] resolved to "+value);
            }
        } else {
            System.out.println("[VtoSVisitor] subSelectResolver is undefined");
        }
        fieldValues.put(columnName, value);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        System.out.println("[VtoSVisitor] CaseExpression");

    }

    @Override
    public void visit(WhenClause whenClause) {
        System.out.println("[VtoSVisitor] WhenClause");

    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        System.out.println("[VtoSVisitor] ExistsExpression");

    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        System.out.println("[VtoSVisitor] AllComparisonExpression");

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        System.out.println("[VtoSVisitor] AnyComparisonExpression");

    }

    @Override
    public void visit(Concat concat) {
        System.out.println("[VtoSVisitor] Concat");

    }

    @Override
    public void visit(Matches matches) {
        System.out.println("[VtoSVisitor] Matches");

    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        System.out.println("[VtoSVisitor] BitwiseAnd");

    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        System.out.println("[VtoSVisitor] BitwiseOr");

    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        System.out.println("[VtoSVisitor] BitwiseXor");

    }

    @Override
    public void visit(CastExpression cast) {
        System.out.println("[VtoSVisitor] CastExpression");

    }

    @Override
    public void visit(Modulo modulo) {
        System.out.println("[VtoSVisitor] Modulo");

    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        System.out.println("[VtoSVisitor] AnalyticExpression");

    }

    @Override
    public void visit(ExtractExpression eexpr) {
        System.out.println("[VtoSVisitor] BitwiseRightShift");

    }

    @Override
    public void visit(IntervalExpression iexpr) {
        System.out.println("[VtoSVisitor] IntervalExpression");

    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        System.out.println("[VtoSVisitor] OracleHierarchicalExpression");

    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        System.out.println("[VtoSVisitor] RegExpMatchOperator");

    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        System.out.println("[VtoSVisitor] JsonExpression");

    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        System.out.println("[VtoSVisitor] JsonOperator");

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        System.out.println("[VtoSVisitor] RegExpMySQLOperator");

    }

    @Override
    public void visit(UserVariable var) {
        System.out.println("[VtoSVisitor] UserVariable");

    }

    @Override
    public void visit(NumericBind bind) {
        System.out.println("[VtoSVisitor] NumericBind");

    }

    @Override
    public void visit(KeepExpression aexpr) {
        System.out.println("[VtoSVisitor] KeepExpression");

    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        System.out.println("[VtoSVisitor] MySQLGroupConcat");

    }

    @Override
    public void visit(ValueListExpression valueList) {
        System.out.println("[VtoSVisitor] ValueListExpression");

    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        System.out.println("[VtoSVisitor] RowConstructor");

    }

    @Override
    public void visit(OracleHint hint) {
        System.out.println("[VtoSVisitor] OracleHint");

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        System.out.println("[VtoSVisitor] TimeKeyExpression");

    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        System.out.println("[VtoSVisitor] DateTimeLiteralExpression");

    }

    @Override
    public void visit(NotExpression aThis) {
        System.out.println("[VtoSVisitor] NotExpression");

    }

    @Override
    public void visit(NextValExpression aThis) {
        System.out.println("[VtoSVisitor] NextValExpression");

    }

    @Override
    public void visit(CollateExpression aThis) {
        System.out.println("[VtoSVisitor] CollateExpression");

    }

    @Override
    public void visit(SimilarToExpression aThis) {
        System.out.println("[VtoSVisitor] SimilarToExpression");

    }

    @Override
    public void visit(ArrayExpression aThis) {
        System.out.println("[VtoSVisitor] ArrayExpression");

    }

    @Override
    public void visit(VariableAssignment aThis) {
        System.out.println("[VtoSVisitor] VariableAssignment");

    }

    @Override
    public void visit(XMLSerializeExpr aThis) {
        System.out.println("[VtoSVisitor] XMLSerializeExpr");

    }
}
