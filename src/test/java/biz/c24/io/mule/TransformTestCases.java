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

public class TransformTestCases extends C24ConnectorTestCase {
    
    @Test
    public void testTransform() throws Exception {
        Customer customer = new Customer();
        customer.initToMinCardinality();
        customer.setName("Al Bundy");
        customer.setCustomerAcronym("AB");
        customer.setCustomerNumber("135");
        customer.setPostZipCode("AA11AA");
        customer.setTelephoneNumber("01234 567890");
        customer.setFaxNumber("01234 567891");
        
        CustomersFile file = new CustomersFile();
        file.addCustomer(customer);
        
        Object obj = runFlow("customersFileTransformFlow", file).getMessage().getPayload();
        
        assertTrue(obj instanceof ContactDetailsFile);
        ContactDetailsFile cdf = (ContactDetailsFile) obj;
        
        assertThat(cdf.getContactDetails().length, is(1));
        
        assertThat(cdf.getContactDetails()[0].getCustomerNumber(), is("135"));

    }

}
