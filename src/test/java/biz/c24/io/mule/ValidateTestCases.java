/*
 * Copyright C24 Technologies Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.c24.io.mule;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

import biz.c24.io.gettingstarted.customer.Address;
import biz.c24.io.gettingstarted.customer.Customer;

public class ValidateTestCases extends C24ConnectorTestCase {
    
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
