package com.ascendix.jdbc.salesforce.statement.processor.utils;

import com.ascendix.jdbc.salesforce.exceptions.UnsupportedArgumentTypeException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Time;

import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ascendix.jdbc.salesforce.statement.processor.InsertQueryProcessor.SF_JDBC_DRIVER_NAME;

public class EvaluateExpressionVisitor implements ExpressionVisitor {

    protected static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);
    protected Map<String, Object> recordFieldsFromDB;
    protected Object result;

    public EvaluateExpressionVisitor(Map<String, Object> recordFieldsFromDB) {
        this.recordFieldsFromDB = recordFieldsFromDB;
    }

    public EvaluateExpressionVisitor subVisitor() {
        return new EvaluateExpressionVisitor(recordFieldsFromDB);
    }

    public Object getResult() {
        return result;
    }

    public String getResultString() {
        return (String) result;
    }

    public String getResultString(String ifNull) {
        if (result == null) {
            return ifNull;
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
        String message = String.format("Cannot convert to Fixed type %s value %s", result.getClass().getName(), result);
        logger.log(Level.SEVERE, message);
        throw new UnsupportedArgumentTypeException(message);
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
        String message = String.format("Cannot convert to Float type %s value %s", result.getClass().getName(), result);
        logger.log(Level.SEVERE, message);
        throw new UnsupportedArgumentTypeException(message);
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

    public boolean isResultDateTime() {
        return result != null && (
                        result instanceof Instant ||
                        result instanceof java.sql.Timestamp);
    }

    public boolean isResultTime() {
        return result != null && (
                        result instanceof java.sql.Time);
    }

    public boolean isResultDate() {
        return result != null && (
                        result instanceof java.sql.Date);
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
            result = new Date();
            return;
        }
        if ("getdate".equalsIgnoreCase(function.getName())) {
            result = LocalDate.now();
            return;
        }
        throw new RuntimeException("Function '"+function.getName()+"' is not implemented.");
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
        EvaluateExpressionVisitor subEvaluatorLeft = subVisitor();
        parenthesis.getExpression().accept(subEvaluatorLeft);
        result = subEvaluatorLeft.getResult();
    }

    @Override
    public void visit(StringValue stringValue) {
        System.out.println("[UpdateVisitor] StringValue="+stringValue.getValue());
        result = stringValue.getValue();
    }

    Object processDateNumberOperation(EvaluateExpressionVisitor left, EvaluateExpressionVisitor right, boolean isAdding) {
        BigDecimal changeValue = BigDecimal.valueOf(right.getResultFloatNumber());
        // https://oracle-base.com/articles/misc/oracle-dates-timestamps-and-intervals
        // Also rounding - because otherwise 1 second will be 0.999993600
        long secondsValue = changeValue.subtract(BigDecimal.valueOf(changeValue.intValue())).multiply(BigDecimal.valueOf(86400), new MathContext(4)).longValue();

        Period changeDays = Period.ofDays(isAdding ? changeValue.intValue() : -changeValue.intValue());
        Duration changeSec = Duration.ofSeconds(isAdding ? secondsValue : -secondsValue);
        if (left.result instanceof java.sql.Date) {
            LocalDate instant = ((java.sql.Date)left.result).toLocalDate();
            instant = instant.plus(changeDays);
            result = java.sql.Date.valueOf(instant);
        }
        if (left.result instanceof Instant) {
            Instant instant = ((Instant)left.result);
            instant = instant.plus(changeDays);
            instant = instant.plus(changeSec);
            result = instant;
        }
        if (left.result instanceof java.sql.Timestamp) {
            Instant instant = ((java.sql.Timestamp)left.result).toInstant();
            instant = instant.plus(changeDays);
            instant = instant.plus(changeSec);
            result = new java.sql.Timestamp(instant.toEpochMilli());
        }
        if (left.result instanceof Time) {
            LocalTime instant = ((java.sql.Time)left.result).toLocalTime();
            instant = instant.plus(changeSec);
            result = java.sql.Time.valueOf(instant);
        }

        return result;
    }

    @Override
    public void visit(Addition addition) {
        System.out.println("[UpdateVisitor] Addition");
        EvaluateExpressionVisitor subEvaluatorLeft = subVisitor();
        addition.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = subVisitor();
        addition.getRightExpression().accept(subEvaluatorRight);

        if (!subEvaluatorLeft.isResultNull() && !subEvaluatorRight.isResultNull()){
            if ((subEvaluatorLeft.isResultDateTime() || subEvaluatorLeft.isResultDate() || subEvaluatorLeft.isResultTime())  &&
                (subEvaluatorRight.isResultFloatNumber() || subEvaluatorRight.isResultFixedNumber()) ) {
                result = processDateNumberOperation(subEvaluatorLeft, subEvaluatorRight, true);
                return;
            }
        }

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
            // if string - convert to string "null"
            result = subEvaluatorLeft.getResultString("null") + subEvaluatorRight.getResultString("null");
            return;
        }

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }
        result = null;
        String message = String.format("Addition not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
        logger.log(Level.SEVERE, message);
        throw new UnsupportedArgumentTypeException(message);
    }

    @Override
    public void visit(Division division) {
        System.out.println("[UpdateVisitor] Division");
        EvaluateExpressionVisitor subEvaluatorLeft = subVisitor();
        division.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = subVisitor();
        division.getRightExpression().accept(subEvaluatorRight);

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }

        boolean isString = subEvaluatorLeft.isResultString()|| subEvaluatorRight.isResultString();
        if (isString) {
            String message = String.format("Division not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
            logger.log(Level.SEVERE, message);
            throw new UnsupportedArgumentTypeException(message);
        }

        if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
            result = subEvaluatorLeft.getResultFloatNumber() / subEvaluatorRight.getResultFloatNumber();
            return;
        }
        if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
            result = subEvaluatorLeft.getResultFixedNumber() / subEvaluatorRight.getResultFixedNumber();
            return;
        }
        result = null;
        String message = String.format("Division not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
        logger.log(Level.SEVERE, message);
        throw new UnsupportedArgumentTypeException(message);
    }

    @Override
    public void visit(IntegerDivision division) {
        System.out.println("[UpdateVisitor] IntegerDivision");
        EvaluateExpressionVisitor subEvaluatorLeft = subVisitor();
        division.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = subVisitor();
        division.getRightExpression().accept(subEvaluatorRight);

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }

        boolean isString = subEvaluatorLeft.isResultString() || subEvaluatorRight.isResultString();
        if (isString) {
            String message = String.format("Division not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
            logger.log(Level.SEVERE, message);
            throw new UnsupportedArgumentTypeException(message);
        }

        if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
            result = Double.doubleToLongBits(subEvaluatorLeft.getResultFloatNumber() / subEvaluatorRight.getResultFloatNumber());
            return;
        }
        if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
            result = subEvaluatorLeft.getResultFixedNumber() / subEvaluatorRight.getResultFixedNumber();
            return;
        }
        String message = String.format("Division not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
        logger.log(Level.SEVERE, message);
        throw new UnsupportedArgumentTypeException(message);
    }

    @Override
    public void visit(Multiplication multiplication) {
        System.out.println("[UpdateVisitor] Multiplication");
        EvaluateExpressionVisitor subEvaluatorLeft = subVisitor();
        multiplication.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = subVisitor();
        multiplication.getRightExpression().accept(subEvaluatorRight);

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }

        boolean isString = subEvaluatorLeft.isResultString() || subEvaluatorRight.isResultString();
        if (isString) {
            String message = String.format("Multiplication not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
            logger.log(Level.SEVERE, message);
            throw new UnsupportedArgumentTypeException(message);
        }

        if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
            result = subEvaluatorLeft.getResultFloatNumber() * subEvaluatorRight.getResultFloatNumber();
            return;
        }
        if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
            result = subEvaluatorLeft.getResultFixedNumber() * subEvaluatorRight.getResultFixedNumber();
            return;
        }
        result = null;
        String message = String.format("Multiplication not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
        logger.log(Level.SEVERE, message);
        throw new UnsupportedArgumentTypeException(message);
    }

    @Override
    public void visit(Subtraction subtraction) {
        System.out.println("[UpdateVisitor] Subtraction");
        EvaluateExpressionVisitor subEvaluatorLeft = subVisitor();
        subtraction.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = subVisitor();
        subtraction.getRightExpression().accept(subEvaluatorRight);

        if (!subEvaluatorLeft.isResultNull() && !subEvaluatorRight.isResultNull()){
            if ((subEvaluatorLeft.isResultDateTime() || subEvaluatorLeft.isResultDate() || subEvaluatorLeft.isResultTime())  &&
                    (subEvaluatorRight.isResultFloatNumber() || subEvaluatorRight.isResultFixedNumber()) ) {
                result = processDateNumberOperation(subEvaluatorLeft, subEvaluatorRight, false);
                return;
            }
        }

        if (subEvaluatorLeft.isResultNull() || subEvaluatorRight.isResultNull()) {
            // if any of the parameters is null - return null
            result = null;
            return;
        }

        boolean isString = subEvaluatorLeft.isResultString() || subEvaluatorRight.isResultString();
        if (isString) {
            String message = String.format("Subtraction not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
            logger.log(Level.SEVERE, message);
            throw new UnsupportedArgumentTypeException(message);
        }

        if (subEvaluatorLeft.isResultFloatNumber() || subEvaluatorRight.isResultFloatNumber()) {
            result = subEvaluatorLeft.getResultFloatNumber() - subEvaluatorRight.getResultFloatNumber();
            return;
        }
        if (subEvaluatorLeft.isResultFixedNumber() || subEvaluatorRight.isResultFixedNumber()) {
            result = subEvaluatorLeft.getResultFixedNumber() - subEvaluatorRight.getResultFixedNumber();
            return;
        }
        String message = String.format("Subtraction not implemented for types %s and %s", subEvaluatorLeft.result.getClass().getName(), subEvaluatorRight.result.getClass().getName());
        logger.log(Level.SEVERE, message);
        result = null;
        throw new UnsupportedArgumentTypeException(message);
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
        EvaluateExpressionVisitor caseEvaluatorLeft = subVisitor();
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
        EvaluateExpressionVisitor subEvaluatorLeft = subVisitor();
        concat.getLeftExpression().accept(subEvaluatorLeft);
        EvaluateExpressionVisitor subEvaluatorRight = subVisitor();
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
