/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */

package biz.c24.io.mule;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.File;
import java.util.List;

import org.junit.Test;

import biz.c24.io.api.C24;
import biz.c24.io.gettingstarted.contact.ContactDetailsFile;
import biz.c24.io.gettingstarted.customer.Customer;
import biz.c24.io.gettingstarted.customer.CustomersFile;

public class TransformAdvancedTestCases extends C24ConnectorTestCase {
    
    /**
     * Checks the result of procesing /Customers.xml with the generateMarketingLinks transform
     */
    private void validateResult(Object result) {
        
        assertTrue(result instanceof List);
        
        List list = (List)result;
        
        assertThat(list.size(), is(2));
        
        assertTrue(list.get(0) instanceof List);
        
        list = (List)list.get(0);
        
        assertThat(list.size(), is(4));
        
        assertTrue(list.get(0) instanceof String);
        
        assertThat((String)list.get(0), is("http://test?campaign=12345&customer=100018"));
   
        list = (List) ((List)result).get(1);
        
        assertThat(list.size(), is(3));
       
        assertThat((String)list.get(0), is("oliver.twist@c24.biz"));
    }
    
    @Test
    public void testTransformAdvanced() throws Exception {
        
        CustomersFile file = C24.parse(CustomersFile.class).from(new File("/Customers.xml"));
        
        Object obj = runFlow("generateMarketingLinksTransformFlow", file).getMessage().getPayload();
        
        validateResult(obj);
    }
    
    @Test
    public void testTransformAdvancedExpansion() throws Exception {
        
        CustomersFile file = C24.parse(CustomersFile.class).from(new File("/Customers.xml"));
        
        Object obj = runFlow("generateMarketingLinksTransformExpandedFlow", file).getMessage().getPayload();
        
        validateResult(obj);
    }

}
