/**
 * Copyright (c) C24 Technologies Limited. All rights reserved.
 */

package biz.c24.io.mule;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import biz.c24.io.gettingstarted.contact.ContactDetailsFile;
import biz.c24.io.gettingstarted.customer.Customer;
import biz.c24.io.gettingstarted.customer.CustomersFile;

public class MarshalTest extends C24ConnectorTestCase {
    
    @Test
    public void testDefaultMarshal() throws Exception {
        Customer customer = new Customer();
        customer.initToMinCardinality();
        customer.setName("Al Bundy");
        customer.setCustomerAcronym("AB");
        customer.setCustomerNumber("135");
        customer.setPostZipCode("AA11AA");
        customer.setTelephoneNumber("01234 567890");
        customer.setFaxNumber("01234 567891");
        
        Object obj = runFlow("marshalFlow", customer).getMessage().getPayload();
        
        assertTrue(obj instanceof String);
        
        assertTrue(((String)obj).startsWith("<"));
    }
    
    @Test
    public void testFormatMarshal() throws Exception {
        Customer customer = new Customer();
        customer.initToMinCardinality();
        customer.setName("Al Bundy");
        customer.setCustomerAcronym("AB");
        customer.setCustomerNumber("135");
        customer.setPostZipCode("AA11AA");
        customer.setTelephoneNumber("01234 567890");
        customer.setFaxNumber("01234 567891");
        
        Object obj = runFlow("jsonMarshalFlow", customer).getMessage().getPayload();
        
        assertTrue(obj instanceof String);
        
        assertTrue(((String)obj).startsWith("{"));
    }

}
