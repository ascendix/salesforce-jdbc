package com.ascendix.jdbc.salesforce.statement.processor.utils;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EvaluateExpressionVisitorTest {

    @Test
    public void testResolveSimpleTypes() {
        Map<String, Object> recordFieldsFromDB = RecordFieldsBuilder.setId("001xx1111111111111").build();

        EvaluateExpressionVisitor visitorS1 = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new StringValue("TestValue-String").accept(visitorS1);
        assertEquals("TestValue-String", visitorS1.getResult());

        EvaluateExpressionVisitor visitorD1 = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new DoubleValue("1.7976931348623157e+308").accept(visitorD1);
        assertEquals(Double.MAX_VALUE, visitorD1.getResult());

        EvaluateExpressionVisitor visitorL1 = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new LongValue("9223372036854775807").accept(visitorL1);
        assertEquals(9223372036854775807L, visitorL1.getResult());
    }

    @Test
    public void testResolveAdditions() {
        Map<String, Object> recordFieldsFromDB = RecordFieldsBuilder.setId("001xx1111111111111").build();

        EvaluateExpressionVisitor visitorSS = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new StringValue("TestPartLeft-"))
                .withRightExpression(new StringValue("TestPartRight~"))
                .accept(visitorSS);
        assertEquals("TestPartLeft-TestPartRight~", visitorSS.getResult());

        EvaluateExpressionVisitor visitorDD = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new DoubleValue("12345678.1231"))
                .withRightExpression(new DoubleValue("88456634.4333"))
                .accept(visitorDD);
        assertEquals(100802312.5564, visitorDD.getResult());

        EvaluateExpressionVisitor visitorLL = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new LongValue("89983422234"))
                .withRightExpression(new LongValue("76463763473"))
                .accept(visitorLL);
        assertEquals(166447185707L, visitorLL.getResult());

        EvaluateExpressionVisitor visitorSNL = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new StringValue("89983422234"))
                .withRightExpression(new LongValue("76463763473"))
                .accept(visitorSNL);
        assertEquals(166447185707L, visitorSNL.getResult());

        EvaluateExpressionVisitor visitorSL = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new StringValue("StrNotNum"))
                .withRightExpression(new LongValue("76463763473"))
                .accept(visitorSL);
        assertEquals("StrNotNum76463763473", visitorSL.getResult());

        EvaluateExpressionVisitor visitorLS = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new LongValue("89983422234"))
                .withRightExpression(new StringValue("76463763473"))
                .accept(visitorLS);
        assertEquals(166447185707L, visitorLS.getResult());

        EvaluateExpressionVisitor visitorLSN = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new LongValue("89983422234"))
                .withRightExpression(new StringValue("StrNotNum"))
                .accept(visitorLSN);
        assertEquals("89983422234StrNotNum", visitorLSN.getResult());

        EvaluateExpressionVisitor visitorDS = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new DoubleValue("12345678.1231"))
                .withRightExpression(new StringValue("88456634.4333"))
                .accept(visitorDS);
        assertEquals(100802312.5564, visitorDS.getResult());

        EvaluateExpressionVisitor visitorDSN = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new DoubleValue("12345678.1231"))
                .withRightExpression(new StringValue("StrNotNum"))
                .accept(visitorDSN);
        assertEquals("1.23456781231E7StrNotNum", visitorDSN.getResult());

        EvaluateExpressionVisitor visitorSD = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new StringValue("12345678.1231"))
                .withRightExpression(new DoubleValue("88456634.4333"))
                .accept(visitorSD);
        assertEquals(100802312.5564, visitorSD.getResult());

        EvaluateExpressionVisitor visitorSND = new EvaluateExpressionVisitor(recordFieldsFromDB);
        new Addition()
                .withLeftExpression( new StringValue("StrNotNum"))
                .withRightExpression(new DoubleValue("88456634.4333"))
                .accept(visitorSND);
        assertEquals("StrNotNum8.84566344333E7", visitorSND.getResult());
    }

    @Test
    public void testResolveFunctions() {
        Map<String, Object> recordFieldsFromDB = RecordFieldsBuilder.setId("001xx1111111111111").build();

        EvaluateExpressionVisitor visitorS1 = new EvaluateExpressionVisitor(recordFieldsFromDB);
        Function func = new Function();
        func.setName("now");
        func.accept(visitorS1);
        assertEquals("TestValue-String", visitorS1.getResult());

    }
}
