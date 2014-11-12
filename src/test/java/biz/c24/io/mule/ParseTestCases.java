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

import java.io.File;
import org.junit.Test;
import org.mule.util.FileUtils;

import biz.c24.io.gettingstarted.customer.CustomersFile;

public class ParseTestCases extends C24ConnectorTestCase {
    
    @Test
    public void testParse() throws Exception {
        String payload = FileUtils.readFileToString(new File("src/test/resources/Customers.xml"));
        Object obj = runFlow("customersFileParseFlow", payload).getMessage().getPayload();
        
        assertTrue(obj instanceof CustomersFile);
        
        CustomersFile cf = (CustomersFile)obj;
        
        assertThat(cf.getCustomer().length, is(4));
    }
    
    @Test
    public void testIllegalParse() throws Exception {
        String payload = "INVALID";
        
        try {
            runFlow("customersFileParseFlow", payload).getMessage().getPayload();
            fail("Illegal message failed to cause parsing exception");
        } catch(C24Exception ex) {
            // Expected behaviour
        }
    }

}
