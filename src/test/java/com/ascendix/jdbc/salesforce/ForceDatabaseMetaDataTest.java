package com.ascendix.jdbc.salesforce;

import static org.junit.Assert.assertEquals;

import java.sql.Types;

import org.junit.Test;

public class ForceDatabaseMetaDataTest {

    @Test
    public void testLookupTypeInfo() {
	ForceDatabaseMetaData.TypeInfo actual = ForceDatabaseMetaData.lookupTypeInfo("int");
	
	assertEquals("int", actual.typeName);
	assertEquals(Types.INTEGER, actual.sqlDataType);
    }

    @Test
    public void testLookupTypeInfo_IfTypeUnknown() {
	ForceDatabaseMetaData.TypeInfo actual = ForceDatabaseMetaData.lookupTypeInfo("my strange type");
	
	assertEquals("default", actual.typeName);
	assertEquals(Types.VARCHAR, actual.sqlDataType);
    }

}
