package com.ascendix.jdbc.salesforce.delegates;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.ascendix.jdbc.salesforce.ForceConnection;
import com.ascendix.jdbc.salesforce.ForceDriver;
import com.sforce.ws.ConnectionException;

public class PartnerServiceTest {

    private ForceConnection connection;
    private PartnerService service;
    
    @Before
    public void setUp() throws Exception {
	Class.forName("com.ascendix.jdbc.salesforce.ForceDriver");
	Properties props = new Properties();
	props.put("user", "vyermakov@ascendix.com.xre.ci");
	props.put("password", "Testdrive1eVQHbJvLGntiiQJd5fxvmPuu");
	connection = (ForceConnection) new ForceDriver().connect("jdbc:ascendix:salesforce://;User=vyermakov@ascendix.com.xre.ci;Password=Testdrive1eVQHbJvLGntiiQJd5fxvmPuu", props);
	service = new PartnerService(connection.getPartnerConnection());
    }

    @Test
    public void testQuery() throws ConnectionException {
	ForceQueryResult result = service.query("SELECT Count(id) FROM Account group by name");
	System.out.println(result.getRecords().get(0).get(1).getValue());
    }

}
