package com.ascendix.jdbc.salesforce.statement.processor.utils;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.update.Update;

import java.util.List;
import java.util.Map;

public class UpdateRecordVisitor implements ExpressionVisitor {

    private Update updateStatement;
    private Map<String, Object> recordFieldsToUpdate;
    private Map<String, Object> recordFieldsFromDB;
    private String columnName;
    private java.util.function.Function<String, List<Map<String, Object>>> subSelectResolver;

    public UpdateRecordVisitor(Update updateStatement, Map<String, Object> recordFieldsToUpdate, Map<String, Object> recordFieldsFromDB, String columnName, java.util.function.Function<String, List<Map<String, Object>>> subSelectResolver) {
        this.updateStatement = updateStatement;
        this.recordFieldsToUpdate = recordFieldsToUpdate;
        this.recordFieldsFromDB = recordFieldsFromDB;
        this.columnName = columnName;
        this.subSelectResolver = subSelectResolver;
    }

    @Override
    public void visit(BitwiseRightShift aThis) {
        System.out.println("[UpdateVisitor] BitwiseRightShift");
    }

    @Override
    public void visit(BitwiseLeftShift aThis) {
        System.out.println("[UpdateVisitor] BitwiseLeftShift");
    }

    @Override
    public void visit(NullValue nullValue) {
        System.out.println("[UpdateVisitor] NullValue column="+columnName);
        recordFieldsToUpdate.put(columnName, null);
    }

    @Override
    public void visit(Function function) {
        System.out.println("[UpdateVisitor] Function function="+function.getName());
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        System.out.println("[UpdateVisitor] SignedExpression");
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        System.out.println("[UpdateVisitor] BitwiseRightShift");
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        System.out.println("[UpdateVisitor] JdbcNamedParameter");
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        System.out.println("[UpdateVisitor] DoubleValue="+doubleValue.getValue()+" column="+columnName);
        recordFieldsToUpdate.put(columnName, doubleValue.getValue());
    }

    @Override
    public void visit(LongValue longValue) {
        System.out.println("[UpdateVisitor] LongValue="+longValue.getValue()+" column="+columnName);
        recordFieldsToUpdate.put(columnName, longValue.getValue());
    }

    @Override
    public void visit(HexValue hexValue) {
        System.out.println("[UpdateVisitor] HexValue="+hexValue.getValue()+" column="+columnName);
        recordFieldsToUpdate.put(columnName, hexValue.getValue());
    }

    @Override
    public void visit(DateValue dateValue) {
        System.out.println("[UpdateVisitor] DateValue column="+columnName);
    }

    @Override
    public void visit(TimeValue timeValue) {
        System.out.println("[UpdateVisitor] BitwiseRightShift");
        recordFieldsToUpdate.put(columnName, timeValue.getValue());
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        System.out.println("[UpdateVisitor] TimestampValue");
        recordFieldsToUpdate.put(columnName, timestampValue.getValue());
    }

    EvaluateExpressionVisitor evaluator() {
        return new EvaluateExpressionVisitor(recordFieldsFromDB);
    }

    private void evaluate(Expression expr) {
        EvaluateExpressionVisitor subEvaluatorLeft = evaluator();
        expr.accept(subEvaluatorLeft);
        recordFieldsToUpdate.put(columnName, subEvaluatorLeft.getResult());
    }


    @Override
    public void visit(Parenthesis parenthesis) {
        System.out.println("[UpdateVisitor] Parenthesis");
        evaluate(parenthesis);
    }

    @Override
    public void visit(StringValue stringValue) {
        System.out.println("[UpdateVisitor] StringValue="+stringValue.getValue()+" column="+columnName);
        recordFieldsToUpdate.put(columnName, stringValue.getValue());
    }

    @Override
    public void visit(Addition addition) {
        System.out.println("[UpdateVisitor] Addition");
        evaluate(addition);
    }

    @Override
    public void visit(Division division) {
        System.out.println("[UpdateVisitor] Division");
        evaluate(division);
    }

    @Override
    public void visit(IntegerDivision division) {
        System.out.println("[UpdateVisitor] IntegerDivision");
        evaluate(division);
    }

    @Override
    public void visit(Multiplication multiplication) {
        System.out.println("[UpdateVisitor] Multiplication");
        evaluate(multiplication);
    }

    @Override
    public void visit(Subtraction subtraction) {
        System.out.println("[UpdateVisitor] Subtraction");
        evaluate(subtraction);
    }

    @Override
    public void visit(AndExpression andExpression) {
        System.out.println("[UpdateVisitor] AndExpression");
        evaluate(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        System.out.println("[UpdateVisitor] OrExpression");
        evaluate(orExpression);
    }

    @Override
    public void visit(Between between) {
        System.out.println("[UpdateVisitor] Between");
        evaluate(between);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        System.out.println("[UpdateVisitor] EqualsTo");
        evaluate(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        System.out.println("[UpdateVisitor] GreaterThan");
        evaluate(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        System.out.println("[UpdateVisitor] GreaterThanEquals");
        evaluate(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        System.out.println("[UpdateVisitor] InExpression");
        evaluate(inExpression);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        System.out.println("[UpdateVisitor] FullTextSearch");
        evaluate(fullTextSearch);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        System.out.println("[UpdateVisitor] IsNullExpression");
        evaluate(isNullExpression);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        System.out.println("[UpdateVisitor] IsBooleanExpression");
        evaluate(isBooleanExpression);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        System.out.println("[UpdateVisitor] LikeExpression");
        evaluate(likeExpression);
    }

    @Override
    public void visit(MinorThan minorThan) {
        System.out.println("[UpdateVisitor] MinorThan");
        evaluate(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        System.out.println("[UpdateVisitor] MinorThanEquals");
        evaluate(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        System.out.println("[UpdateVisitor] NotEqualsTo");
        evaluate(notEqualsTo);
    }

    @Override
    public void visit(Column tableColumn) {
        System.out.println("[UpdateVisitor] Column column="+tableColumn.getColumnName());
        evaluate(tableColumn);
    }

    @Override
    public void visit(SubSelect subSelect) {
        System.out.println("[VtoxSVisitor] SubSelect="+subSelect.toString()+" column="+columnName);
//        Object value = null;
//        if (subSelectResolver != null) {
//            subSelect.setUseBrackets(false);
//            List<Map<String, Object>> records = subSelectResolver.apply(subSelect.toString());
//            if (records.size() == 1 && records.get(0).size() == 1) {
//                 return the value as plain value
//                value = records.get(0).entrySet().iterator().next().getValue();
//                System.out.println("[UpdateVisitor] resolved to "+value);
//            }
//        } else {
//            System.out.println("[UpdateVisitor] subSelectResolver is undefined");
//        }
//        recordFieldsToUpdate.put(columnName, value);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        System.out.println("[UpdateVisitor] CaseExpression");
        evaluate(caseExpression);
    }

    @Override
    public void visit(WhenClause whenClause) {
        System.out.println("[UpdateVisitor] WhenClause");
        evaluate(whenClause);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        System.out.println("[UpdateVisitor] ExistsExpression");
        evaluate(existsExpression);
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        System.out.println("[UpdateVisitor] AllComparisonExpression");
        evaluate(allComparisonExpression);
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        System.out.println("[UpdateVisitor] AnyComparisonExpression");
        evaluate(anyComparisonExpression);
    }

    @Override
    public void visit(Concat concat) {
        System.out.println("[UpdateVisitor] Concat");
        evaluate(concat);
    }

    @Override
    public void visit(Matches matches) {
        System.out.println("[UpdateVisitor] Matches");
        evaluate(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        System.out.println("[UpdateVisitor] BitwiseAnd");
        evaluate(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr aThis) {
        System.out.println("[UpdateVisitor] BitwiseOr");
        evaluate(aThis);
    }

    @Override
    public void visit(BitwiseXor aThis) {
        System.out.println("[UpdateVisitor] BitwiseXor");
        evaluate(aThis);
    }

    @Override
    public void visit(CastExpression aThis) {
        System.out.println("[UpdateVisitor] CastExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(Modulo aThis) {
        System.out.println("[UpdateVisitor] Modulo");
        evaluate(aThis);
    }

    @Override
    public void visit(AnalyticExpression aThis) {
        System.out.println("[UpdateVisitor] AnalyticExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(ExtractExpression aThis) {
        System.out.println("[UpdateVisitor] BitwiseRightShift");
        evaluate(aThis);
    }

    @Override
    public void visit(IntervalExpression aThis) {
        System.out.println("[UpdateVisitor] IntervalExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(OracleHierarchicalExpression aThis) {
        System.out.println("[UpdateVisitor] OracleHierarchicalExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(RegExpMatchOperator aThis) {
        System.out.println("[UpdateVisitor] RegExpMatchOperator");
        evaluate(aThis);
    }

    @Override
    public void visit(JsonExpression aThis) {
        System.out.println("[UpdateVisitor] JsonExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(JsonOperator aThis) {
        System.out.println("[UpdateVisitor] JsonOperator");
        evaluate(aThis);
    }

    @Override
    public void visit(RegExpMySQLOperator aThis) {
        System.out.println("[UpdateVisitor] RegExpMySQLOperator");
        evaluate(aThis);
    }

    @Override
    public void visit(UserVariable aThis) {
        System.out.println("[UpdateVisitor] UserVariable");
        evaluate(aThis);
    }

    @Override
    public void visit(NumericBind aThis) {
        System.out.println("[UpdateVisitor] NumericBind");
        evaluate(aThis);
    }

    @Override
    public void visit(KeepExpression aThis) {
        System.out.println("[UpdateVisitor] KeepExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(MySQLGroupConcat aThis) {
        System.out.println("[UpdateVisitor] MySQLGroupConcat");
        evaluate(aThis);
    }

    @Override
    public void visit(ValueListExpression aThis) {
        System.out.println("[UpdateVisitor] ValueListExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(RowConstructor aThis) {
        System.out.println("[UpdateVisitor] RowConstructor");
        evaluate(aThis);
    }

    @Override
    public void visit(OracleHint aThis) {
        System.out.println("[UpdateVisitor] OracleHint");
        evaluate(aThis);
    }

    @Override
    public void visit(TimeKeyExpression aThis) {
        System.out.println("[UpdateVisitor] TimeKeyExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(DateTimeLiteralExpression aThis) {
        System.out.println("[UpdateVisitor] DateTimeLiteralExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(NotExpression aThis) {
        System.out.println("[UpdateVisitor] NotExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(NextValExpression aThis) {
        System.out.println("[UpdateVisitor] NextValExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(CollateExpression aThis) {
        System.out.println("[UpdateVisitor] CollateExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(SimilarToExpression aThis) {
        System.out.println("[UpdateVisitor] SimilarToExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(ArrayExpression aThis) {
        System.out.println("[UpdateVisitor] ArrayExpression");
        evaluate(aThis);
    }

    @Override
    public void visit(VariableAssignment aThis) {
        System.out.println("[UpdateVisitor] VariableAssignment");
        evaluate(aThis);
    }

    @Override
    public void visit(XMLSerializeExpr aThis) {
        System.out.println("[UpdateVisitor] XMLSerializeExpr");
        evaluate(aThis);
    }
}
