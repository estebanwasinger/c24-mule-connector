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
import java.util.List;

import org.junit.Test;

import biz.c24.io.api.C24;
import biz.c24.io.gettingstarted.customer.CustomersFile;

public class TransformAdvancedTestCases extends C24ConnectorTestCase {
    
    /**
     * Checks the result of procesing /Customers.xml with the generateMarketingLinks transform
     */
    private void validateResult(Object result) {
        
        assertTrue(result instanceof List);
        
        List<?> list = (List<?>)result;
        
        assertThat(list.size(), is(2));
        
        assertTrue(list.get(0) instanceof List);
        
        list = (List<?>)list.get(0);
        
        assertThat(list.size(), is(4));
        
        assertTrue(list.get(0) instanceof String);
        
        assertThat((String)list.get(0), is("http://test?campaign=12345&customer=100018"));
   
        list = (List<?>) ((List<?>)result).get(1);
        
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
