package com.ascendix.jdbc.salesforce.metadata;

import com.ascendix.jdbc.salesforce.connection.ForceConnection;
import com.ascendix.jdbc.salesforce.metadata.ForceDatabaseMetaData;
import org.junit.Test;

import java.sql.Types;

import static org.junit.Assert.assertEquals;

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

        assertEquals("other", actual.typeName);
        assertEquals(Types.OTHER, actual.sqlDataType);
    }

}
