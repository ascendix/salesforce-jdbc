package com.ascendix.jdbc.salesforce.statement.processor.utils;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ascendix.jdbc.salesforce.statement.processor.InsertQueryProcessor.SF_JDBC_DRIVER_NAME;

public class EvaluateExpressionVisitor implements ExpressionVisitor {

    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    private Map<String, Object> recordFieldsFromDB;
    private Object result;

    public EvaluateExpressionVisitor(Map<String, Object> recordFieldsFromDB) {
        this.recordFieldsFromDB = recordFieldsFromDB;
    }

    public Object getResult() {
        return result;
    }

    public String getResultString() {
        return (String) result;
    }

    public String getResultString(String ifNull) {
        if (result == null) {
            return "";
        }
        if (result instanceof String) return ((String) result);
        return result.toString();
    }

    public long getResultFixedNumber() {
        if (result == null) {
            return 0;
        }
        if (result instanceof Double) return ((Double) result).longValue();
        if (result instanceof Float) return ((Float) result).longValue();
        if (result instanceof Long) return ((Long) result);
        if (result instanceof Integer) return ((Integer) result).longValue();
        if (result instanceof String) return Long.parseLong((String)result);
        logger.log(Level.SEVERE, String.format("Cannot convert to Fixed type %s value %s", result.getClass().getName(), result));
        return 0;
    }

    public double getResultFloatNumber() {
        if (result == null) {
            return 0d;
        }
        if (result instanceof Double) return (Double) result;
        if (result instanceof Float) return ((Float) result).doubleValue();
        if (result instanceof Long) return ((Long) result).doubleValue();
        if (result instanceof Integer) return ((Integer) result).doubleValue();
        if (result instanceof String) return Double.parseDouble((String)result);
        logger.log(Level.SEVERE, String.format("Cannot convert to Float type %s value %s", result.getClass().getName(), result));
        return 0d;
    }

    public boolean isResultString() {
        return result != null && result instanceof String;
    }

    public boolean isResultFixedNumber() {
        return result != null && (
                        result instanceof Long ||
                        result instanceof Integer);
    }

    public boolean isResultFloatNumber() {
        return result != null && (
                        result instanceof Double ||
                        result instanceof Float);
    }

    public boolean isResultNull() {
        return result == null;
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
        System.out.println("[UpdateVisitor] NullValue");
        result = null;
    }

    @Override
    public void visit(Function function) {
        System.out.println("[UpdateVisitor] Function function="+function.getName());
        if ("now".equalsIgnoreCase(function.getName())) {
            result = new Date().toGMTString();
        }
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
        System.out.println("[UpdateVisitor] DoubleValue="+doubleValue.getValue());
        result = doubleValue.getValue();
    }

    @Override
    public void visit(LongValue longValue) {
        System.out.println("[UpdateVisitor] LongValue="+longValue.getValue());
        result = longValue.getValue();
    }

    @Override
    public void visit(HexValue hexValue) {
        System.out.println("[UpdateVisitor] HexValue="+hexValue.getValue());
        result = hexValue.getValue();
    }

    @Override
    public void visit(DateValue dateValue) {
        System.out.println("[UpdateVisitor] DateValue ");
        result = dateValue.getValue();
    }

    @Override
    public void visit(TimeValue timeValue) {
        System.out.println("[UpdateVisitor] BitwiseRightShift");
        result = timeValue.getValue();
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        System.out.println("[UpdateVisitor] TimestampValue");
        result = timestampValue.getValue();
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        System.out.println("[UpdateVisitor] Parenthesis");
        EvaluateExpressionVisitor subEvaluatorLeft = new EvaluateExpressionVisitor(recordFieldsFromDB);
        parenthesis.getExpression().accept(subEvaluatorLeft);
        result = subEvaluatorLeft.getResult();
    }

    @Override
    public void visit(StringValue stringValue) {
        System.out.println("[UpdateVisitor] StringValue="+stringValue.getValue());
        result = stringValue.getValue();
    }

    @Override
    public void visit(Addition addition) {
        System.out.println("[UpdateVisitor] Addition");
        EvaluateExpressionVisitor subEvaluatorLeft = new EvaluateExpressionVisitor(recordFieldsFromDB);
        addition.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = new EvaluateExpressionVisitor(recordFieldsFromDB);
        addition.getRightExpression().accept(subEvaluatorRight);

        boolean isString = subEvaluatorLeft.isResultString() || subEvaluatorRight.isResultString();
        try {
            if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
                result = subEvaluatorLeft.getResultFloatNumber() + subEvaluatorRight.getResultFloatNumber();
                return;
            }
            if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
                result = subEvaluatorLeft.getResultFixedNumber() + subEvaluatorRight.getResultFixedNumber();
                return;
            }
        } catch (NumberFormatException e) {
            isString = true;
        }

        if (isString) {
            result = subEvaluatorLeft.getResultString("null") + subEvaluatorRight.getResultString("null");
            return;
        }

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }
        logger.log(Level.SEVERE, String.format("Addition not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
        result = null;
    }

    @Override
    public void visit(Division division) {
        System.out.println("[UpdateVisitor] Division");
        EvaluateExpressionVisitor subEvaluatorLeft = new EvaluateExpressionVisitor(recordFieldsFromDB);
        division.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = new EvaluateExpressionVisitor(recordFieldsFromDB);
        division.getRightExpression().accept(subEvaluatorRight);

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }

        boolean isString = subEvaluatorLeft.isResultString()|| subEvaluatorRight.isResultString();
        if (isString) {
            logger.log(Level.SEVERE, String.format("Division not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
            return;
        }

        if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
            result = subEvaluatorLeft.getResultFloatNumber() / subEvaluatorRight.getResultFloatNumber();
            return;
        }
        if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
            result = subEvaluatorLeft.getResultFixedNumber() / subEvaluatorRight.getResultFixedNumber();
            return;
        }
        logger.log(Level.SEVERE, String.format("Division not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
        result = null;
    }

    @Override
    public void visit(IntegerDivision division) {
        System.out.println("[UpdateVisitor] IntegerDivision");
        EvaluateExpressionVisitor subEvaluatorLeft = new EvaluateExpressionVisitor(recordFieldsFromDB);
        division.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = new EvaluateExpressionVisitor(recordFieldsFromDB);
        division.getRightExpression().accept(subEvaluatorRight);

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }

        boolean isString = subEvaluatorLeft.isResultString() || subEvaluatorRight.isResultString();
        if (isString) {
            logger.log(Level.SEVERE, String.format("Division not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
            return;
        }

        if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
            result = Double.doubleToLongBits(subEvaluatorLeft.getResultFloatNumber() / subEvaluatorRight.getResultFloatNumber());
            return;
        }
        if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
            result = subEvaluatorLeft.getResultFixedNumber() / subEvaluatorRight.getResultFixedNumber();
            return;
        }
        logger.log(Level.SEVERE, String.format("Division not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
        result = null;
    }

    @Override
    public void visit(Multiplication multiplication) {
        System.out.println("[UpdateVisitor] Multiplication");
        EvaluateExpressionVisitor subEvaluatorLeft = new EvaluateExpressionVisitor(recordFieldsFromDB);
        multiplication.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = new EvaluateExpressionVisitor(recordFieldsFromDB);
        multiplication.getRightExpression().accept(subEvaluatorRight);

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }

        boolean isString = subEvaluatorLeft.isResultString() || subEvaluatorRight.isResultString();
        if (isString) {
            logger.log(Level.SEVERE, String.format("Multiplication not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
            return;
        }

        if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
            result = subEvaluatorLeft.getResultFloatNumber() * subEvaluatorRight.getResultFloatNumber();
            return;
        }
        if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
            result = subEvaluatorLeft.getResultFixedNumber() * subEvaluatorRight.getResultFixedNumber();
            return;
        }
        logger.log(Level.SEVERE, String.format("Multiplication not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
        result = null;
    }

    @Override
    public void visit(Subtraction subtraction) {
        System.out.println("[UpdateVisitor] Subtraction");
        EvaluateExpressionVisitor subEvaluatorLeft = new EvaluateExpressionVisitor(recordFieldsFromDB);
        subtraction.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = new EvaluateExpressionVisitor(recordFieldsFromDB);
        subtraction.getRightExpression().accept(subEvaluatorRight);

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }

        boolean isString = subEvaluatorLeft.isResultString() || subEvaluatorRight.isResultString();
        if (isString) {
            logger.log(Level.SEVERE, String.format("Subtraction not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
            return;
        }

        if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
            result = subEvaluatorLeft.getResultFloatNumber() - subEvaluatorRight.getResultFloatNumber();
            return;
        }
        if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
            result = subEvaluatorLeft.getResultFixedNumber() - subEvaluatorRight.getResultFixedNumber();
            return;
        }
        logger.log(Level.SEVERE, String.format("Subtraction not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName()));
        result = null;
    }

    @Override
    public void visit(AndExpression andExpression) {
        System.out.println("[UpdateVisitor] AndExpression");

    }

    @Override
    public void visit(OrExpression orExpression) {
        System.out.println("[UpdateVisitor] OrExpression");

    }

    @Override
    public void visit(Between between) {
        System.out.println("[UpdateVisitor] Between");

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        System.out.println("[UpdateVisitor] EqualsTo");
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        System.out.println("[UpdateVisitor] GreaterThan");

    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        System.out.println("[UpdateVisitor] GreaterThanEquals");

    }

    @Override
    public void visit(InExpression inExpression) {
        System.out.println("[UpdateVisitor] InExpression");

    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        System.out.println("[UpdateVisitor] FullTextSearch");

    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        System.out.println("[UpdateVisitor] IsNullExpression");

    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        System.out.println("[UpdateVisitor] IsBooleanExpression");

    }

    @Override
    public void visit(LikeExpression likeExpression) {
        System.out.println("[UpdateVisitor] LikeExpression");

    }

    @Override
    public void visit(MinorThan minorThan) {
        System.out.println("[UpdateVisitor] MinorThan");

    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        System.out.println("[UpdateVisitor] MinorThanEquals");

    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        System.out.println("[UpdateVisitor] NotEqualsTo");

    }

    @Override
    public void visit(Column tableColumn) {
        System.out.println("[UpdateVisitor] Column column="+tableColumn.getColumnName());
        result = recordFieldsFromDB.get(tableColumn.getColumnName());
    }

    @Override
    public void visit(SubSelect subSelect) {
        System.out.println("[VtoxSVisitor] SubSelect="+subSelect.toString());
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        System.out.println("[UpdateVisitor] CaseExpression");
        EvaluateExpressionVisitor caseEvaluatorLeft = new EvaluateExpressionVisitor(recordFieldsFromDB);
        caseExpression.getSwitchExpression().accept(caseEvaluatorLeft);

    }

    @Override
    public void visit(WhenClause whenClause) {
        System.out.println("[UpdateVisitor] WhenClause");
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        System.out.println("[UpdateVisitor] ExistsExpression");
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        System.out.println("[UpdateVisitor] AllComparisonExpression");

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        System.out.println("[UpdateVisitor] AnyComparisonExpression");

    }

    @Override
    public void visit(Concat concat) {
        System.out.println("[UpdateVisitor] Concat");
        EvaluateExpressionVisitor subEvaluatorLeft = new EvaluateExpressionVisitor(recordFieldsFromDB);
        concat.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = new EvaluateExpressionVisitor(recordFieldsFromDB);
        concat.getRightExpression().accept(subEvaluatorRight);

        result = subEvaluatorLeft.getResultString("null") + subEvaluatorRight.getResultString("null");
    }

    @Override
    public void visit(Matches matches) {
        System.out.println("[UpdateVisitor] Matches");

    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        System.out.println("[UpdateVisitor] BitwiseAnd");

    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        System.out.println("[UpdateVisitor] BitwiseOr");

    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        System.out.println("[UpdateVisitor] BitwiseXor");

    }

    @Override
    public void visit(CastExpression cast) {
        System.out.println("[UpdateVisitor] CastExpression");

    }

    @Override
    public void visit(Modulo modulo) {
        System.out.println("[UpdateVisitor] Modulo");

    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        System.out.println("[UpdateVisitor] AnalyticExpression");

    }

    @Override
    public void visit(ExtractExpression eexpr) {
        System.out.println("[UpdateVisitor] BitwiseRightShift");

    }

    @Override
    public void visit(IntervalExpression iexpr) {
        System.out.println("[UpdateVisitor] IntervalExpression");

    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        System.out.println("[UpdateVisitor] OracleHierarchicalExpression");

    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        System.out.println("[UpdateVisitor] RegExpMatchOperator");

    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        System.out.println("[UpdateVisitor] JsonExpression");

    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        System.out.println("[UpdateVisitor] JsonOperator");

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        System.out.println("[UpdateVisitor] RegExpMySQLOperator");

    }

    @Override
    public void visit(UserVariable var) {
        System.out.println("[UpdateVisitor] UserVariable");

    }

    @Override
    public void visit(NumericBind bind) {
        System.out.println("[UpdateVisitor] NumericBind");

    }

    @Override
    public void visit(KeepExpression aexpr) {
        System.out.println("[UpdateVisitor] KeepExpression");

    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        System.out.println("[UpdateVisitor] MySQLGroupConcat");

    }

    @Override
    public void visit(ValueListExpression valueList) {
        System.out.println("[UpdateVisitor] ValueListExpression");

    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        System.out.println("[UpdateVisitor] RowConstructor");

    }

    @Override
    public void visit(OracleHint hint) {
        System.out.println("[UpdateVisitor] OracleHint");

    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        System.out.println("[UpdateVisitor] TimeKeyExpression");

    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        System.out.println("[UpdateVisitor] DateTimeLiteralExpression");

    }

    @Override
    public void visit(NotExpression aThis) {
        System.out.println("[UpdateVisitor] NotExpression");

    }

    @Override
    public void visit(NextValExpression aThis) {
        System.out.println("[UpdateVisitor] NextValExpression");

    }

    @Override
    public void visit(CollateExpression aThis) {
        System.out.println("[UpdateVisitor] CollateExpression");

    }

    @Override
    public void visit(SimilarToExpression aThis) {
        System.out.println("[UpdateVisitor] SimilarToExpression");

    }

    @Override
    public void visit(ArrayExpression aThis) {
        System.out.println("[UpdateVisitor] ArrayExpression");

    }

    @Override
    public void visit(VariableAssignment aThis) {
        System.out.println("[UpdateVisitor] VariableAssignment");

    }

    @Override
    public void visit(XMLSerializeExpr aThis) {
        System.out.println("[UpdateVisitor] XMLSerializeExpr");

    }
}
