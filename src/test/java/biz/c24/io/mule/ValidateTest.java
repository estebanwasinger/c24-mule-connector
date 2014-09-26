package biz.c24.io.mule;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import biz.c24.io.gettingstarted.customer.Address;
import biz.c24.io.gettingstarted.customer.Customer;

public class ValidateTest extends C24ConnectorTestCase {
    
    @Test
    public void testValid() throws Exception {
        Customer customer = new Customer();
        customer.initToMinCardinality();
        customer.setName("Al Bundy");
        customer.setCustomerAcronym("AB");
        customer.setCustomerNumber("1");
        customer.setPostZipCode("AA11AA");
        customer.setTelephoneNumber("01234 567890");
        customer.setFaxNumber("01234 567891");
        
        Object obj = runFlow("validateFlow", customer).getMessage().getPayload();
        
        assertTrue(obj instanceof Customer);
        assertThat(((Customer)obj).getName(), is(customer.getName()));
    }
    
    @Test
    public void testInvalid() throws Exception {
        Customer customer = new Customer();
        customer.initToMinCardinality();
        customer.setName("Al Bundy");
        // Invalid acronym
        customer.setCustomerAcronym("aB");
        customer.setCustomerNumber("1");
        customer.setPostZipCode("AA11AA");
        customer.setTelephoneNumber("01234 567890");
        customer.setFaxNumber("01234 567891");
        
        try {
            Object obj = runFlow("validateFlow", customer).getMessage().getPayload();
            fail("Invalid message did not generate an exception");
        } catch(C24Exception ex) {
            // Expected behaviour
        }
    }

}
