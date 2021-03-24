package com.ascendix.jdbc.salesforce.statement.processor.utils;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.Set;
import java.util.logging.Logger;

import static com.ascendix.jdbc.salesforce.statement.processor.InsertQueryProcessor.SF_JDBC_DRIVER_NAME;

public class ColumnsFinderVisitor implements ExpressionVisitor {
    private static final Logger logger = Logger.getLogger(SF_JDBC_DRIVER_NAME);

    private final Set<String> columns;
    private boolean functionFound = false;

    public ColumnsFinderVisitor(Set<String> columns) {
        this.columns = columns;
    }

    public boolean isFunctionFound() {
        return functionFound;
    }

    private void addColumn(Column column) {
        if (columns.add(column.getColumnName())) {
            logger.info("New column found: "+column.getColumnName());
        } else {
            logger.info("Already detected column: "+column.getColumnName());
        }
    }

    private void processExpression(Expression expr) {
        expr.accept(this);
    }

    private void processBinaryExpression(BinaryExpression expr) {
        expr.getLeftExpression().accept(this);
        expr.getRightExpression().accept(this);
    }

    @Override
    public void visit(BitwiseRightShift aThis) {
        System.out.println("[ColumnsFinder] BitwiseRightShift");
        processBinaryExpression(aThis);
    }

    @Override
    public void visit(BitwiseLeftShift aThis) {
        System.out.println("[ColumnsFinder] BitwiseLeftShift");
        processBinaryExpression(aThis);
    }

    @Override
    public void visit(NullValue nullValue) {
        System.out.println("[ColumnsFinder] NullValue ");
    }

    @Override
    public void visit(Function function) {
        System.out.println("[ColumnsFinder] Function");
        functionFound = true;
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        System.out.println("[ColumnsFinder] SignedExpression");
        signedExpression.getExpression().accept(this);
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {
        System.out.println("[ColumnsFinder] JdbcParameter");
    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {
        System.out.println("[ColumnsFinder] JdbcNamedParameter");
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        System.out.println("[ColumnsFinder] DoubleValue");
    }

    @Override
    public void visit(LongValue longValue) {
        System.out.println("[ColumnsFinder] LongValue");
    }

    @Override
    public void visit(HexValue hexValue) {
        System.out.println("[ColumnsFinder] HexValue");
    }

    @Override
    public void visit(DateValue dateValue) {
        System.out.println("[ColumnsFinder] DateValue");
    }

    @Override
    public void visit(TimeValue timeValue) {
        System.out.println("[ColumnsFinder] BitwiseRightShift");
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        System.out.println("[ColumnsFinder] TimestampValue");
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        System.out.println("[ColumnsFinder] Parenthesis");
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {
        System.out.println("[ColumnsFinder] StringValue");
    }

    @Override
    public void visit(Addition addition) {
        System.out.println("[ColumnsFinder] Addition");
        processBinaryExpression(addition);
    }

    @Override
    public void visit(Division division) {
        System.out.println("[ColumnsFinder] Division");
        processBinaryExpression(division);
    }

    @Override
    public void visit(IntegerDivision division) {
        System.out.println("[ColumnsFinder] IntegerDivision");
        processBinaryExpression(division);
    }

    @Override
    public void visit(Multiplication multiplication) {
        System.out.println("[ColumnsFinder] Multiplication");
        processBinaryExpression(multiplication);
    }

    @Override
    public void visit(Subtraction subtraction) {
        System.out.println("[ColumnsFinder] Subtraction");
        processBinaryExpression(subtraction);
    }

    @Override
    public void visit(AndExpression andExpression) {
        System.out.println("[ColumnsFinder] AndExpression");
        processBinaryExpression(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        System.out.println("[ColumnsFinder] OrExpression");
        processBinaryExpression(orExpression);
    }

    @Override
    public void visit(Between between) {
        System.out.println("[ColumnsFinder] Between");
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        System.out.println("[ColumnsFinder] EqualsTo");
        processBinaryExpression(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        System.out.println("[ColumnsFinder] GreaterThan");
        processBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        System.out.println("[ColumnsFinder] GreaterThanEquals");
        processBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        System.out.println("[ColumnsFinder] InExpression");
        inExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {
        System.out.println("[ColumnsFinder] FullTextSearch");
        fullTextSearch.getMatchColumns().forEach( this::addColumn );
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {
        System.out.println("[ColumnsFinder] IsNullExpression");
        isNullExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {
        System.out.println("[ColumnsFinder] IsBooleanExpression");
        isBooleanExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(LikeExpression likeExpression) {
        System.out.println("[ColumnsFinder] LikeExpression");
        processExpression(likeExpression);
    }

    @Override
    public void visit(MinorThan minorThan) {
        System.out.println("[ColumnsFinder] MinorThan");
        processBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        System.out.println("[ColumnsFinder] MinorThanEquals");
        processBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        System.out.println("[ColumnsFinder] NotEqualsTo");
        processBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(Column tableColumn) {
        System.out.println("[ColumnsFinder] Column");
        addColumn(tableColumn);
    }

    @Override
    public void visit(SubSelect subSelect) {
        System.out.println("[VtoxSVisitor] SubSelect="+subSelect.toString());
//        Object value = null;
//        if (subSelectResolver != null) {
//            subSelect.setUseBrackets(false);
//            List<Map<String, Object>> records = subSelectResolver.apply(subSelect.toString());
//            if (records.size() == 1 && records.get(0).size() == 1) {
//                 return the value as plain value
//                value = records.get(0).entrySet().iterator().next().getValue();
//                System.out.println("[ColumnsFinder] resolved to "+value);
//            }
//        } else {
//            System.out.println("[ColumnsFinder] subSelectResolver is undefined");
//        }
//        recordFieldsToUpdate.put(columnName, value);
    }

    @Override
    public void visit(CaseExpression caseExpression) {
        System.out.println("[ColumnsFinder] CaseExpression NOT_SUPPORTED");
    }

    @Override
    public void visit(WhenClause whenClause) {
        System.out.println("[ColumnsFinder] WhenClause");

    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        System.out.println("[ColumnsFinder] ExistsExpression");
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        System.out.println("[ColumnsFinder] AllComparisonExpression NOT_SUPPORTED!!!");

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        System.out.println("[ColumnsFinder] AnyComparisonExpression");

    }

    @Override
    public void visit(Concat concat) {
        System.out.println("[ColumnsFinder] Concat");
        processBinaryExpression(concat);
    }

    @Override
    public void visit(Matches matches) {
        System.out.println("[ColumnsFinder] Matches");
        processBinaryExpression(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        System.out.println("[ColumnsFinder] BitwiseAnd");
        processBinaryExpression(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        System.out.println("[ColumnsFinder] BitwiseOr");
        processBinaryExpression(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        System.out.println("[ColumnsFinder] BitwiseXor");
        processBinaryExpression(bitwiseXor);
    }

    @Override
    public void visit(CastExpression cast) {
        System.out.println("[ColumnsFinder] CastExpression");
        cast.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Modulo modulo) {
        System.out.println("[ColumnsFinder] Modulo");
        processBinaryExpression(modulo);
    }

    @Override
    public void visit(AnalyticExpression aexpr) {
        System.out.println("[ColumnsFinder] AnalyticExpression");
        aexpr.getExpression().accept(this);
    }

    @Override
    public void visit(ExtractExpression eexpr) {
        System.out.println("[ColumnsFinder] BitwiseRightShift");
        eexpr.getExpression().accept(this);
    }

    @Override
    public void visit(IntervalExpression iexpr) {
        System.out.println("[ColumnsFinder] IntervalExpression");
        iexpr.getExpression().accept(this);
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        System.out.println("[ColumnsFinder] OracleHierarchicalExpression");
        oexpr.getStartExpression().accept(this);
        oexpr.getConnectExpression().accept(this);
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        System.out.println("[ColumnsFinder] RegExpMatchOperator");
        processBinaryExpression(rexpr);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {
        System.out.println("[ColumnsFinder] JsonExpression");
        addColumn(jsonExpr.getColumn());
    }

    @Override
    public void visit(JsonOperator jsonExpr) {
        System.out.println("[ColumnsFinder] JsonOperator");
        processBinaryExpression(jsonExpr);
    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {
        System.out.println("[ColumnsFinder] RegExpMySQLOperator");
        processBinaryExpression(regExpMySQLOperator);
    }

    @Override
    public void visit(UserVariable var) {
        System.out.println("[ColumnsFinder] UserVariable");

    }

    @Override
    public void visit(NumericBind bind) {
        System.out.println("[ColumnsFinder] NumericBind");
    }

    @Override
    public void visit(KeepExpression aexpr) {
        System.out.println("[ColumnsFinder] KeepExpression");

    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {
        System.out.println("[ColumnsFinder] MySQLGroupConcat");
        groupConcat.getExpressionList().getExpressions().forEach( expression ->  expression.accept(this));
    }

    @Override
    public void visit(ValueListExpression valueList) {
        System.out.println("[ColumnsFinder] ValueListExpression");
        valueList.getExpressionList().getExpressions().forEach( expression ->  expression.accept(this));
    }

    @Override
    public void visit(RowConstructor rowConstructor) {
        System.out.println("[ColumnsFinder] RowConstructor");
        rowConstructor.getExprList().getExpressions().forEach( expression ->  expression.accept(this));
    }

    @Override
    public void visit(OracleHint hint) {
        System.out.println("[ColumnsFinder] OracleHint");
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {
        System.out.println("[ColumnsFinder] TimeKeyExpression");
    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {
        System.out.println("[ColumnsFinder] DateTimeLiteralExpression");
    }

    @Override
    public void visit(NotExpression aThis) {
        System.out.println("[ColumnsFinder] NotExpression");
        aThis.getExpression().accept(this);
    }

    @Override
    public void visit(NextValExpression aThis) {
        System.out.println("[ColumnsFinder] NextValExpression");
    }

    @Override
    public void visit(CollateExpression aThis) {
        System.out.println("[ColumnsFinder] CollateExpression");
        aThis.getLeftExpression().accept(this);
    }

    @Override
    public void visit(SimilarToExpression aThis) {
        System.out.println("[ColumnsFinder] SimilarToExpression");
        processBinaryExpression(aThis);
    }

    @Override
    public void visit(ArrayExpression aThis) {
        System.out.println("[ColumnsFinder] ArrayExpression");
        aThis.getIndexExpression().accept(this);
        aThis.getObjExpression().accept(this);
    }

    @Override
    public void visit(VariableAssignment aThis) {
        System.out.println("[ColumnsFinder] VariableAssignment");
        aThis.getExpression().accept(this);
    }

    @Override
    public void visit(XMLSerializeExpr aThis) {
        System.out.println("[ColumnsFinder] XMLSerializeExpr");
        aThis.getExpression().accept(this);
    }
}
